package br.com.leonardodasilvasousa.biblioteca_api.reserva.exception;

public class ReservaDuplicadaException extends RuntimeException {

    public ReservaDuplicadaException(Long livroId, Long leitorId) {
        super("Leitor já possui reserva ativa para este livro. livroId=" + livroId + ", leitorId=" + leitorId);
    }
}
