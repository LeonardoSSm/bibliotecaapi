package br.com.leonardodasilvasousa.biblioteca_api.reserva.exception;

public class LivroIndisponivelException extends RuntimeException {

    public LivroIndisponivelException(Long livroId) {
        super("Livro indisponível para reserva. ID: " + livroId);
    }
}
