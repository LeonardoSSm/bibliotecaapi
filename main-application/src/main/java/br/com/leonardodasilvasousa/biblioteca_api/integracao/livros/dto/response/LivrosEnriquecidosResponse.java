package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.response;

import java.util.List;

public record LivrosEnriquecidosResponse(
        String query,
        Integer totalResultados,
        List<LivroEnriquecidoResponse> resultados
) {
}
