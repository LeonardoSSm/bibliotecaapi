package br.com.leonardodasilvasousa.biblioteca_api.livro.exception;

public class LivroNaoEncontradoException extends RuntimeException {

    public LivroNaoEncontradoException(Long id) {
        super("Livro não encontrado. ID: " + id);
    }
}
