package br.com.leonardodasilvasousa.biblioteca_api.reserva.repository;

import br.com.leonardodasilvasousa.biblioteca_api.reserva.model.Reserva;
import br.com.leonardodasilvasousa.biblioteca_api.reserva.model.ReservaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByLivroIdAndLeitorIdAndStatus(Long livroId, Long leitorId, ReservaStatus status);

    boolean existsByLivroIdAndStatus(Long livroId, ReservaStatus status);
}
