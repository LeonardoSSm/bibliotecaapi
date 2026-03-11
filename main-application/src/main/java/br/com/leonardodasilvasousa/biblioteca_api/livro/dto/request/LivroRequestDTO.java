package br.com.leonardodasilvasousa.biblioteca_api.livro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LivroRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        @NotBlank(message = "Autor é obrigatório")
        @Size(max = 255, message = "Autor deve ter no máximo 255 caracteres")
        String autor,

        @NotBlank(message = "ISBN é obrigatório")
        @Size(min = 10, max = 17, message = "ISBN deve ter entre 10 e 17 caracteres")
        String isbn,

        @NotBlank(message = "Categoria é obrigatória")
        @Size(max = 255, message = "Categoria deve ter no máximo 255 caracteres")
        String categoria,

        @Min(value = 1, message = "Quantidade total deve ser no mínimo 1")
        Integer quantidadeTotal,

        @Min(value = 0, message = "Quantidade disponível não pode ser negativa")
        Integer quantidadeDisponivel
) {
}
