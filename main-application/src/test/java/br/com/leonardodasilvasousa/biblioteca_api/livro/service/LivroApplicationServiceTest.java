package br.com.leonardodasilvasousa.biblioteca_api.livro.service;

import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.request.LivroRequestDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.dto.response.LivroResponseDTO;
import br.com.leonardodasilvasousa.biblioteca_api.livro.exception.LivroNaoEncontradoException;
import br.com.leonardodasilvasousa.biblioteca_api.livro.model.Livro;
import br.com.leonardodasilvasousa.biblioteca_api.livro.repository.LivroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivroApplicationServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroApplicationService livroApplicationService;

    @Test
    @DisplayName("Deve criar livro mapeando RequestDTO para entidade e ResponseDTO")
    void deveCriarLivroComMapeamentoDeDtos() {
        LivroRequestDTO request = new LivroRequestDTO(
                "Domain-Driven Design",
                "Eric Evans",
                "9780321125217",
                "Arquitetura",
                10,
                6
        );

        Livro salvo = new Livro(
                1L,
                request.titulo(),
                request.autor(),
                request.isbn(),
                request.categoria(),
                request.quantidadeTotal(),
                request.quantidadeDisponivel()
        );

        when(livroRepository.findByIsbn(request.isbn())).thenReturn(Optional.empty());
        when(livroRepository.save(any(Livro.class))).thenReturn(salvo);

        LivroResponseDTO response = livroApplicationService.criarLivro(request);

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("Domain-Driven Design", response.titulo()),
                () -> assertEquals("9780321125217", response.isbn()),
                () -> assertEquals(6, response.quantidadeDisponivel())
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando quantidade disponível for maior que quantidade total")
    void deveLancarErroQuandoQuantidadeDisponivelForMaiorQueTotal() {
        LivroRequestDTO requestInvalido = new LivroRequestDTO(
                "Livro X",
                "Autor Y",
                "9780000000000",
                "Teste",
                3,
                5
        );

        assertThrows(IllegalArgumentException.class,
                () -> livroApplicationService.criarLivro(requestInvalido));
    }

    @Test
    @DisplayName("Deve atualizar livro existente com dados do RequestDTO")
    void deveAtualizarLivroExistente() {
        Long id = 10L;

        Livro existente = new Livro(id, "Livro antigo", "Autor", "9780132350884", "Dev", 4, 2);
        LivroRequestDTO request = new LivroRequestDTO("Livro novo", "Novo autor", "9780132350884", "Tech", 7, 3);

        when(livroRepository.findById(id)).thenReturn(Optional.of(existente));
        when(livroRepository.findByIsbn(request.isbn())).thenReturn(Optional.of(existente));
        when(livroRepository.save(any(Livro.class))).thenAnswer(inv -> inv.getArgument(0));

        LivroResponseDTO response = livroApplicationService.atualizarLivro(id, request);

        assertAll(
                () -> assertEquals("Livro novo", response.titulo()),
                () -> assertEquals("Novo autor", response.autor()),
                () -> assertEquals(7, response.quantidadeTotal()),
                () -> assertEquals(3, response.quantidadeDisponivel())
        );
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar livro inexistente")
    void deveLancarErroAoBuscarLivroInexistente() {
        when(livroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class,
                () -> livroApplicationService.buscarPorId(999L));
    }
}
