package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.service;

import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.client.GoogleBooksClient;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.client.OpenLibraryClient;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google.GoogleBookItem;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google.GoogleBooksVolumesResponse;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google.GoogleIndustryIdentifier;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.google.GoogleVolumeInfo;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.openlibrary.OpenLibraryDoc;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.external.openlibrary.OpenLibrarySearchResponse;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.response.LivroEnriquecidoResponse;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.response.LivrosEnriquecidosResponse;
import br.com.leonardodasilvasousa.biblioteca_api.livro.model.Livro;
import br.com.leonardodasilvasousa.biblioteca_api.livro.repository.LivroRepository;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.model.ReservaStatus;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LivrosIntegracaoService {

    private static final int MAX_RESULTADOS_PADRAO = 10;
    private static final int MAX_RESULTADOS_PERMITIDO = 20;

    private static final Pattern ANO_PATTERN = Pattern.compile("(\\d{4})");

    private final GoogleBooksClient googleBooksClient;
    private final OpenLibraryClient openLibraryClient;
    private final LivroRepository livroRepository;
    private final ReservaRepository reservaRepository;

    private final String googleApiKey;

    public LivrosIntegracaoService(GoogleBooksClient googleBooksClient,
                                   OpenLibraryClient openLibraryClient,
                                   LivroRepository livroRepository,
                                   ReservaRepository reservaRepository,
                                   @Value("${app.integracoes.google-books.api-key:}") String googleApiKey) {
        this.googleBooksClient = googleBooksClient;
        this.openLibraryClient = openLibraryClient;
        this.livroRepository = livroRepository;
        this.reservaRepository = reservaRepository;
        this.googleApiKey = googleApiKey;
    }

    public LivrosEnriquecidosResponse buscarLivrosEnriquecidos(String query, Integer max) {
        String termo = normalizarQuery(query);
        int limite = normalizarLimite(max);

        GoogleBooksVolumesResponse volumesResponse = googleBooksClient.buscarVolumes(
                termo,
                limite,
                googleApiKey == null || googleApiKey.isBlank() ? null : googleApiKey
        );

        List<GoogleBookItem> volumes = Optional.ofNullable(volumesResponse)
                .map(GoogleBooksVolumesResponse::items)
                .orElse(List.of());

        Map<String, OpenLibraryDoc> openLibraryPorIsbn = buscarOpenLibraryPorIsbn(termo, limite);

        List<LivroEnriquecidoResponse> resultados = volumes.stream()
                .map(volume -> montarRespostaEnriquecida(volume, openLibraryPorIsbn))
                .sorted(Comparator.comparing(LivroEnriquecidoResponse::scoreRecomendacao).reversed())
                .limit(limite)
                .toList();

        return new LivrosEnriquecidosResponse(termo, resultados.size(), resultados);
    }

    private Map<String, OpenLibraryDoc> buscarOpenLibraryPorIsbn(String termo, int limite) {
        try {
            OpenLibrarySearchResponse response = openLibraryClient.buscarLivros(termo, limite * 2);

            return Optional.ofNullable(response)
                    .map(OpenLibrarySearchResponse::docs)
                    .orElse(List.of())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(doc -> doc.isbn() != null)
                    .flatMap(doc -> doc.isbn().stream()
                            .map(this::normalizarIsbn)
                            .filter(isbn -> !isbn.isBlank())
                            .map(isbn -> Map.entry(isbn, doc)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (anterior, atual) -> anterior));
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private LivroEnriquecidoResponse montarRespostaEnriquecida(GoogleBookItem item,
                                                               Map<String, OpenLibraryDoc> openLibraryPorIsbn) {
        GoogleVolumeInfo volumeInfo = item != null ? item.volumeInfo() : null;

        String isbn13 = extrairIsbn13(volumeInfo);
        OpenLibraryDoc openLibraryDoc = isbn13 != null ? openLibraryPorIsbn.get(normalizarIsbn(isbn13)) : null;

        String titulo = obterTitulo(volumeInfo, openLibraryDoc);
        List<String> autores = obterAutores(volumeInfo, openLibraryDoc);
        List<String> categorias = obterCategorias(volumeInfo, openLibraryDoc);
        Integer anoPublicacao = obterAnoPublicacao(volumeInfo, openLibraryDoc);

        double notaExterna = Optional.ofNullable(volumeInfo)
                .map(GoogleVolumeInfo::averageRating)
                .orElse(0.0);

        int totalAvaliacoes = Optional.ofNullable(volumeInfo)
                .map(GoogleVolumeInfo::ratingsCount)
                .orElse(0);

        Livro livroInterno = Optional.ofNullable(isbn13)
                .flatMap(livroRepository::findByIsbn)
                .orElse(null);

        int quantidadeDisponivelInterna = livroInterno != null ? livroInterno.getQuantidadeDisponivel() : 0;
        boolean reservaAtivaInterna = livroInterno != null
                && reservaRepository.existsByLivroIdAndStatus(livroInterno.getId(), ReservaStatus.ATIVA);

        int scoreRecomendacao = calcularScore(
                quantidadeDisponivelInterna,
                reservaAtivaInterna,
                notaExterna,
                totalAvaliacoes,
                anoPublicacao,
                isbn13,
                autores,
                categorias
        );

        List<String> fontes = new ArrayList<>();
        fontes.add("GOOGLE_BOOKS");

        if (openLibraryDoc != null) {
            fontes.add("OPEN_LIBRARY");
        }

        if (livroInterno != null) {
            fontes.add("INTERNO");
        }

        return new LivroEnriquecidoResponse(
                isbn13,
                titulo,
                autores,
                categorias,
                anoPublicacao,
                notaExterna,
                totalAvaliacoes,
                quantidadeDisponivelInterna,
                reservaAtivaInterna,
                scoreRecomendacao,
                fontes
        );
    }

    private String obterTitulo(GoogleVolumeInfo volumeInfo, OpenLibraryDoc openLibraryDoc) {
        if (volumeInfo != null && volumeInfo.title() != null && !volumeInfo.title().isBlank()) {
            return volumeInfo.title();
        }

        if (openLibraryDoc != null && openLibraryDoc.title() != null && !openLibraryDoc.title().isBlank()) {
            return openLibraryDoc.title();
        }

        return "Sem titulo informado";
    }

    private List<String> obterAutores(GoogleVolumeInfo volumeInfo, OpenLibraryDoc openLibraryDoc) {
        if (volumeInfo != null && volumeInfo.authors() != null && !volumeInfo.authors().isEmpty()) {
            return volumeInfo.authors();
        }

        if (openLibraryDoc != null && openLibraryDoc.authorName() != null && !openLibraryDoc.authorName().isEmpty()) {
            return openLibraryDoc.authorName();
        }

        return List.of();
    }

    private List<String> obterCategorias(GoogleVolumeInfo volumeInfo, OpenLibraryDoc openLibraryDoc) {
        if (volumeInfo != null && volumeInfo.categories() != null && !volumeInfo.categories().isEmpty()) {
            return volumeInfo.categories();
        }

        if (openLibraryDoc != null && openLibraryDoc.subject() != null && !openLibraryDoc.subject().isEmpty()) {
            return openLibraryDoc.subject().stream().limit(3).toList();
        }

        return List.of();
    }

    private Integer obterAnoPublicacao(GoogleVolumeInfo volumeInfo, OpenLibraryDoc openLibraryDoc) {
        if (volumeInfo != null && volumeInfo.publishedDate() != null) {
            Matcher matcher = ANO_PATTERN.matcher(volumeInfo.publishedDate());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        if (openLibraryDoc != null && openLibraryDoc.firstPublishYear() != null) {
            return openLibraryDoc.firstPublishYear();
        }

        return null;
    }

    private String extrairIsbn13(GoogleVolumeInfo volumeInfo) {
        if (volumeInfo == null || volumeInfo.industryIdentifiers() == null) {
            return null;
        }

        Optional<String> isbn13 = volumeInfo.industryIdentifiers().stream()
                .filter(Objects::nonNull)
                .filter(id -> id.type() != null && "ISBN_13".equalsIgnoreCase(id.type()))
                .map(GoogleIndustryIdentifier::identifier)
                .map(this::normalizarIsbn)
                .filter(id -> id.length() == 13)
                .findFirst();

        if (isbn13.isPresent()) {
            return isbn13.get();
        }

        return volumeInfo.industryIdentifiers().stream()
                .filter(Objects::nonNull)
                .map(GoogleIndustryIdentifier::identifier)
                .map(this::normalizarIsbn)
                .filter(id -> id.length() == 13)
                .findFirst()
                .orElse(null);
    }

    private String normalizarIsbn(String isbn) {
        if (isbn == null) {
            return "";
        }

        return isbn.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizarQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query de busca é obrigatória");
        }

        return query.trim();
    }

    private int normalizarLimite(Integer max) {
        if (max == null || max <= 0) {
            return MAX_RESULTADOS_PADRAO;
        }

        return Math.min(max, MAX_RESULTADOS_PERMITIDO);
    }

    private int calcularScore(int quantidadeDisponivelInterna,
                              boolean reservaAtivaInterna,
                              double notaExterna,
                              int totalAvaliacoes,
                              Integer anoPublicacao,
                              String isbn13,
                              List<String> autores,
                              List<String> categorias) {
        int score = 0;

        if (quantidadeDisponivelInterna > 0) {
            score += 40;
        }

        if (reservaAtivaInterna) {
            score -= 10;
        }

        score += (int) Math.min(25, Math.round((notaExterna / 5.0) * 25));
        score += Math.min(20, totalAvaliacoes / 10);

        if (anoPublicacao != null) {
            int anoAtual = Year.now().getValue();
            if (anoPublicacao >= anoAtual - 5) {
                score += 10;
            } else if (anoPublicacao >= anoAtual - 10) {
                score += 6;
            } else {
                score += 3;
            }
        }

        int metadadosCompletos = 0;
        if (isbn13 != null && !isbn13.isBlank()) {
            metadadosCompletos++;
        }

        if (autores != null && !autores.isEmpty()) {
            metadadosCompletos++;
        }

        if (categorias != null && !categorias.isEmpty()) {
            metadadosCompletos++;
        }

        if (anoPublicacao != null) {
            metadadosCompletos++;
        }

        score += Math.min(5, metadadosCompletos);

        return Math.max(0, Math.min(100, score));
    }
}
