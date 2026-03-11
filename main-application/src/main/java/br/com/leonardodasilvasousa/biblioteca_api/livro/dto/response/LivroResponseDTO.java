package br.com.leonardodasilvasousa.biblioteca_api.livro.dto.response;

public record LivroResponseDTO(
        Long id,
        String titulo,
        String autor,
        String isbn,
        String categoria,
        Integer quantidadeTotal,
        Integer quantidadeDisponivel
) {
}
