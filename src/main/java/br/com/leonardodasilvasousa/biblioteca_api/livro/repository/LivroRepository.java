package br.com.leonardodasilvasousa.biblioteca_api.livro.repository;

import br.com.leonardodasilvasousa.biblioteca_api.livro.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}
