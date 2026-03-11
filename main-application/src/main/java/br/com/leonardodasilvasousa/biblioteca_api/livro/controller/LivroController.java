package br.com.leonardodasilvasousa.biblioteca_api.livro.controller;

import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.request.LivroRequestDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.response.LivroResponseDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.service.LivroApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/livros")
public class LivroController {

    private final LivroApplicationService livroApplicationService;

    public LivroController(LivroApplicationService livroApplicationService) {
        this.livroApplicationService = livroApplicationService;
    }

    @GetMapping
    public List<LivroResponseDTO> listarLivros() {
        return livroApplicationService.listarLivros();
    }

    @GetMapping("/{id}")
    public LivroResponseDTO buscarPorId(@PathVariable Long id) {
        return livroApplicationService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroResponseDTO criarLivro(@Valid @RequestBody LivroRequestDTO requestDTO) {
        return livroApplicationService.criarLivro(requestDTO);
    }

    @PutMapping("/{id}")
    public LivroResponseDTO atualizarLivro(@PathVariable Long id,
                                           @Valid @RequestBody LivroRequestDTO requestDTO) {
        return livroApplicationService.atualizarLivro(id, requestDTO);
    }
}
