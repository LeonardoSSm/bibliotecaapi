package br.com.leonardodasilvasousa.biblioteca_api.livro.service;

import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.request.LivroRequestDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.response.LivroResponseDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.exception.LivroNaoEncontradoException;
import br.com.leonardodasilvasousa.biblioteca_api.livro.model.Livro;
import br.com.leonardodasilvasousa.biblioteca_api.livro.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LivroApplicationService {

    private final LivroRepository livroRepository;

    public LivroApplicationService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    @Transactional(readOnly = true)
    public List<LivroResponseDTO> listarLivros() {
        return livroRepository.findAll().stream()
                .map(this::mapearParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LivroResponseDTO buscarPorId(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(id));

        return mapearParaResponse(livro);
    }

    public LivroResponseDTO criarLivro(LivroRequestDTO requestDTO) {
        validarQuantidades(requestDTO.quantidadeTotal(), requestDTO.quantidadeDisponivel());

        livroRepository.findByIsbn(requestDTO.isbn())
                .ifPresent(livro -> {
                    throw new IllegalArgumentException("Já existe livro com o ISBN informado");
                });

        Livro livro = new Livro(
                null,
                requestDTO.titulo(),
                requestDTO.autor(),
                requestDTO.isbn(),
                requestDTO.categoria(),
                requestDTO.quantidadeTotal(),
                requestDTO.quantidadeDisponivel()
        );

        return mapearParaResponse(livroRepository.save(livro));
    }

    public LivroResponseDTO atualizarLivro(Long id, LivroRequestDTO requestDTO) {
        validarQuantidades(requestDTO.quantidadeTotal(), requestDTO.quantidadeDisponivel());

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(id));

        livroRepository.findByIsbn(requestDTO.isbn())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new IllegalArgumentException("Já existe outro livro com o ISBN informado");
                    }
                });

        livro.setTitulo(requestDTO.titulo());
        livro.setAutor(requestDTO.autor());
        livro.setIsbn(requestDTO.isbn());
        livro.setCategoria(requestDTO.categoria());
        livro.setQuantidadeTotal(requestDTO.quantidadeTotal());
        livro.setQuantidadeDisponivel(requestDTO.quantidadeDisponivel());

        return mapearParaResponse(livroRepository.save(livro));
    }

    private void validarQuantidades(Integer quantidadeTotal, Integer quantidadeDisponivel) {
        if (quantidadeTotal == null || quantidadeDisponivel == null) {
            throw new IllegalArgumentException("Quantidade total e disponível são obrigatórias");
        }

        if (quantidadeDisponivel > quantidadeTotal) {
            throw new IllegalArgumentException("Quantidade disponível não pode ser maior que a quantidade total");
        }
    }

    private LivroResponseDTO mapearParaResponse(Livro livro) {
        return new LivroResponseDTO(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.getCategoria(),
                livro.getQuantidadeTotal(),
                livro.getQuantidadeDisponivel()
        );
    }
}
