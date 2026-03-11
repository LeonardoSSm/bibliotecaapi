package br.com.leonardodasilvasousa.biblioteca_api.integracao.livros.dto.response;

import java.util.List;

public record LivroEnriquecidoResponse(
        String isbn13,
        String titulo,
        List<String> autores,
        List<String> categorias,
        Integer anoPublicacao,
        Double notaExterna,
        Integer totalAvaliacoes,
        Integer quantidadeDisponivelInterna,
        Boolean reservaAtivaInterna,
        Integer scoreRecomendacao,
        List<String> fontesUtilizadas
) {
}
