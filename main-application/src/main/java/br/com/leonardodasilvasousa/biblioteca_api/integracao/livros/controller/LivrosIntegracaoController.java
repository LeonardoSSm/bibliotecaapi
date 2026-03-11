package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.controller;

import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.response.LivrosEnriquecidosResponse;
import br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.service.LivrosIntegracaoService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/livros")
@Validated
public class LivrosIntegracaoController {

    private final LivrosIntegracaoService livrosIntegracaoService;

    public LivrosIntegracaoController(LivrosIntegracaoService livrosIntegracaoService) {
        this.livrosIntegracaoService = livrosIntegracaoService;
    }

    @GetMapping("/enriquecidos")
    public LivrosEnriquecidosResponse buscarLivrosEnriquecidos(
            @RequestParam("query") @NotBlank String query,
            @RequestParam(value = "max", defaultValue = "10") @Min(1) @Max(20) Integer max
    ) {
        return livrosIntegracaoService.buscarLivrosEnriquecidos(query, max);
    }
}
