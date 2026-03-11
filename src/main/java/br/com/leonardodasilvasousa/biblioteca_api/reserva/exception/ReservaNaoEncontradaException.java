package br.com.leonardodasilvasousa.biblioteca_api.reserva.exception;

public class ReservaNaoEncontradaException extends RuntimeException {

    public ReservaNaoEncontradaException(Long reservaId) {
        super("Reserva não encontrada. ID: " + reservaId);
    }
}
