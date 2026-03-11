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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ReservaService {

    private static final int PRAZO_PADRAO_RESERVA_DIAS = 7;

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final LeitorRepository leitorRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          LivroRepository livroRepository,
                          LeitorRepository leitorRepository) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
    }

    public Reserva criarReserva(Long livroId, Long leitorId) {
        Livro livro = buscarLivroPorId(livroId);
        Leitor leitor = buscarLeitorPorId(leitorId);

        validarReservaDuplicada(livroId, leitorId);
        validarLivroDisponivel(livro, livroId);

        livro.reduzirQuantidadeDisponivel();
        livroRepository.save(livro);

        Reserva reserva = Reserva.criarAtiva(livro, leitor, LocalDate.now(), PRAZO_PADRAO_RESERVA_DIAS);
        return reservaRepository.save(reserva);
    }

    public Reserva cancelarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));

        if (ReservaStatus.ATIVA.equals(reserva.getStatus())) {
            reserva.cancelar();
            Livro livro = reserva.getLivro();
            livro.aumentarQuantidadeDisponivel();
            livroRepository.save(livro);
        }

        return reservaRepository.save(reserva);
    }

    private Livro buscarLivroPorId(Long livroId) {
        return livroRepository.findById(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado. ID: " + livroId));
    }

    private Leitor buscarLeitorPorId(Long leitorId) {
        return leitorRepository.findById(leitorId)
                .orElseThrow(() -> new IllegalArgumentException("Leitor não encontrado. ID: " + leitorId));
    }

    private void validarReservaDuplicada(Long livroId, Long leitorId) {
        boolean possuiReservaAtiva = reservaRepository.existsByLivroIdAndLeitorIdAndStatus(
                livroId,
                leitorId,
                ReservaStatus.ATIVA
        );

        if (possuiReservaAtiva) {
            throw new ReservaDuplicadaException(livroId, leitorId);
        }
    }

    private void validarLivroDisponivel(Livro livro, Long livroId) {
        if (livro.getQuantidadeDisponivel() <= 0) {
            throw new LivroIndisponivelException(livroId);
        }
    }
}
