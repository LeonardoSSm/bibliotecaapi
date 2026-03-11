package br.com.leonardodasilvasousa.biblioteca_api.leitor.repository;

import br.com.leonardodasilvasousa.biblioteca_api.leitor.model.Leitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeitorRepository extends JpaRepository<Leitor, Long> {
}
