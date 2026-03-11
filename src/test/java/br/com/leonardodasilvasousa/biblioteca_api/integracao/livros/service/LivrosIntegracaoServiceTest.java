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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivrosIntegracaoServiceTest {

    @Mock
    private GoogleBooksClient googleBooksClient;

    @Mock
    private OpenLibraryClient openLibraryClient;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private LivrosIntegracaoService livrosIntegracaoService;

    @Test
    @DisplayName("Deve consolidar dados externos com disponibilidade interna e calcular score")
    void deveConsolidarDadosExternosComInternos() {
        when(livroRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        GoogleVolumeInfo googleVolumeInfo = new GoogleVolumeInfo(
                "Clean Architecture",
                List.of("Robert C. Martin"),
                List.of("Software Architecture"),
                4.5,
                120,
                "2023-02-10",
                List.of(new GoogleIndustryIdentifier("ISBN_13", "9780134494166"))
        );

        GoogleBooksVolumesResponse googleResponse = new GoogleBooksVolumesResponse(
                List.of(new GoogleBookItem(googleVolumeInfo))
        );

        OpenLibraryDoc openLibraryDoc = new OpenLibraryDoc(
                "Clean Architecture",
                List.of("Robert C. Martin"),
                List.of("9780134494166"),
                List.of("Software", "Architecture"),
                2017
        );

        Livro livroInterno = new Livro(
                1L,
                "Clean Architecture",
                "Robert C. Martin",
                "9780134494166",
                "Tecnologia",
                5,
                3
        );

        when(googleBooksClient.buscarVolumes("clean architecture", 10, null)).thenReturn(googleResponse);
        when(openLibraryClient.buscarLivros("clean architecture", 20))
                .thenReturn(new OpenLibrarySearchResponse(List.of(openLibraryDoc)));
        when(livroRepository.findByIsbn("9780134494166")).thenReturn(Optional.of(livroInterno));
        when(reservaRepository.existsByLivroIdAndStatus(1L, ReservaStatus.ATIVA)).thenReturn(true);

        LivrosEnriquecidosResponse response = livrosIntegracaoService.buscarLivrosEnriquecidos("clean architecture", 10);
        LivroEnriquecidoResponse livro = response.resultados().getFirst();

        assertAll(
                () -> assertEquals(1, response.totalResultados()),
                () -> assertEquals("9780134494166", livro.isbn13()),
                () -> assertEquals(3, livro.quantidadeDisponivelInterna()),
                () -> assertEquals(true, livro.reservaAtivaInterna()),
                () -> assertEquals(79, livro.scoreRecomendacao()),
                () -> assertEquals(List.of("GOOGLE_BOOKS", "OPEN_LIBRARY", "INTERNO"), livro.fontesUtilizadas())
        );
    }

    @Test
    @DisplayName("Deve continuar retornando resultados quando Open Library estiver indisponível")
    void deveRetornarResultadosMesmoComFalhaOpenLibrary() {
        when(livroRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        GoogleVolumeInfo googleVolumeInfo = new GoogleVolumeInfo(
                "Effective Java",
                List.of("Joshua Bloch"),
                List.of("Java"),
                4.7,
                90,
                "2019",
                List.of(new GoogleIndustryIdentifier("ISBN_13", "9780134685991"))
        );

        when(googleBooksClient.buscarVolumes("effective java", 10, null)).thenReturn(
                new GoogleBooksVolumesResponse(List.of(new GoogleBookItem(googleVolumeInfo)))
        );

        when(openLibraryClient.buscarLivros("effective java", 20))
                .thenThrow(new RuntimeException("OpenLibrary indisponível"));

        LivrosEnriquecidosResponse response = livrosIntegracaoService.buscarLivrosEnriquecidos("effective java", 10);
        LivroEnriquecidoResponse livro = response.resultados().getFirst();

        assertAll(
                () -> assertEquals(1, response.totalResultados()),
                () -> assertEquals(List.of("GOOGLE_BOOKS"), livro.fontesUtilizadas())
        );
    }

    @Test
    @DisplayName("Deve limitar quantidade de retorno e ordenar por score de recomendação")
    void deveLimitarERetornarOrdenadoPorScore() {
        when(livroRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        GoogleVolumeInfo altoScore = new GoogleVolumeInfo(
                "Livro A",
                List.of("Autor A"),
                List.of("Categoria A"),
                4.8,
                220,
                "2025",
                List.of(new GoogleIndustryIdentifier("ISBN_13", "9780000000001"))
        );

        GoogleVolumeInfo baixoScore = new GoogleVolumeInfo(
                "Livro B",
                List.of("Autor B"),
                List.of("Categoria B"),
                1.0,
                5,
                "2000",
                List.of(new GoogleIndustryIdentifier("ISBN_13", "9780000000002"))
        );

        GoogleVolumeInfo medioScore = new GoogleVolumeInfo(
                "Livro C",
                List.of("Autor C"),
                List.of("Categoria C"),
                3.0,
                30,
                "2018",
                List.of(new GoogleIndustryIdentifier("ISBN_13", "9780000000003"))
        );

        when(googleBooksClient.buscarVolumes("arquitetura", 2, null)).thenReturn(
                new GoogleBooksVolumesResponse(List.of(
                        new GoogleBookItem(baixoScore),
                        new GoogleBookItem(altoScore),
                        new GoogleBookItem(medioScore)
                ))
        );

        when(openLibraryClient.buscarLivros("arquitetura", 4)).thenReturn(new OpenLibrarySearchResponse(List.of()));

        LivrosEnriquecidosResponse response = livrosIntegracaoService.buscarLivrosEnriquecidos("arquitetura", 2);

        assertAll(
                () -> assertEquals(2, response.resultados().size()),
                () -> assertEquals("Livro A", response.resultados().get(0).titulo()),
                () -> assertEquals("Livro C", response.resultados().get(1).titulo())
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando query estiver em branco")
    void deveLancarErroParaQueryInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> livrosIntegracaoService.buscarLivrosEnriquecidos("   ", 10));
    }
}
