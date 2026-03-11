package br.com.leonardodasilvasousa.biblioteca_api.reserva.service;

import br.com.leonardodasilvasousa.biblioteca_api.leitor.model.Leitor;
import br.com.leonardodasilvasousa.biblioteca_api.leitor.repository.LeitorRepository;
import br.com.leonardodasilvasousa.biblioteca_api.livro.model.Livro;
import br.com.leonardodasilvasousa.biblioteca_api.livro.repository.LivroRepository;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.exception.LivroIndisponivelException;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.exception.ReservaDuplicadaException;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.exception.ReservaNaoEncontradaException;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.model.Reserva;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.model.ReservaStatus;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LeitorRepository leitorRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    @DisplayName("Deve criar reserva ativa com prazo padrão e reduzir quantidade disponível do livro")
    void deveCriarReservaAtivaQuandoLivroDisponivel() {
        Long livroId = 10L;
        Long leitorId = 20L;

        Livro livro = novoLivro(livroId, 5, 2);
        Leitor leitor = novoLeitor(leitorId);

        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(leitorId)).thenReturn(Optional.of(leitor));
        when(reservaRepository.existsByLivroIdAndLeitorIdAndStatus(livroId, leitorId, ReservaStatus.ATIVA))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaService.criarReserva(livroId, leitorId);

        assertAll(
                () -> assertEquals(ReservaStatus.ATIVA, reserva.getStatus()),
                () -> assertEquals(7, reserva.getPrazoDias()),
                () -> assertEquals(LocalDate.now(), reserva.getDataReserva()),
                () -> assertEquals(1, livro.getQuantidadeDisponivel())
        );

        verify(reservaRepository).save(any(Reserva.class));
        verify(livroRepository).save(livro);
    }

    @Test
    @DisplayName("Deve lançar erro ao reservar livro sem quantidade disponível")
    void deveLancarErroQuandoLivroIndisponivel() {
        Long livroId = 10L;
        Long leitorId = 20L;

        Livro livro = novoLivro(livroId, 5, 0);
        Leitor leitor = novoLeitor(leitorId);

        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(leitorId)).thenReturn(Optional.of(leitor));
        when(reservaRepository.existsByLivroIdAndLeitorIdAndStatus(livroId, leitorId, ReservaStatus.ATIVA))
                .thenReturn(false);

        assertThrows(LivroIndisponivelException.class, () -> reservaService.criarReserva(livroId, leitorId));

        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar criar reserva ativa duplicada para o mesmo livro e leitor")
    void deveLancarErroQuandoReservaDuplicada() {
        Long livroId = 10L;
        Long leitorId = 20L;

        Livro livro = novoLivro(livroId, 5, 3);
        Leitor leitor = novoLeitor(leitorId);

        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(leitorId)).thenReturn(Optional.of(leitor));
        when(reservaRepository.existsByLivroIdAndLeitorIdAndStatus(livroId, leitorId, ReservaStatus.ATIVA))
                .thenReturn(true);

        assertThrows(ReservaDuplicadaException.class, () -> reservaService.criarReserva(livroId, leitorId));

        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    @DisplayName("Deve cancelar reserva ativa e devolver uma unidade para quantidade disponível")
    void deveCancelarReservaAtivaComSucesso() {
        Long reservaId = 30L;
        Livro livro = novoLivro(10L, 5, 1);
        Leitor leitor = novoLeitor(20L);
        Reserva reserva = new Reserva(reservaId, livro, leitor, LocalDate.now(), ReservaStatus.ATIVA, 7);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reservaCancelada = reservaService.cancelarReserva(reservaId);

        assertAll(
                () -> assertEquals(ReservaStatus.CANCELADA, reservaCancelada.getStatus()),
                () -> assertEquals(2, livro.getQuantidadeDisponivel())
        );

        verify(livroRepository).save(livro);
        verify(reservaRepository).save(reserva);
    }

    @Test
    @DisplayName("Deve lançar erro ao cancelar uma reserva inexistente")
    void deveLancarErroQuandoReservaNaoEncontradaNoCancelamento() {
        Long reservaId = 999L;
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

        assertThrows(ReservaNaoEncontradaException.class, () -> reservaService.cancelarReserva(reservaId));

        verify(livroRepository, never()).save(any(Livro.class));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    private Livro novoLivro(Long id, int quantidadeTotal, int quantidadeDisponivel) {
        return new Livro(id, "Clean Code", "Robert C. Martin", "9780132350884", "Engenharia de Software", quantidadeTotal,
                quantidadeDisponivel);
    }

    private Leitor novoLeitor(Long id) {
        return new Leitor(id, "Ana Clara", "ana.clara@universidade.edu", "202300123");
    }
}
