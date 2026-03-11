package br.com.leonardodasilvasousa.biblioteca_api.reserva.model;

import br.com.leonardodasilvasousa.biblioteca_api.leitor.model.Leitor;
import br.com.leonardodasilvasousa.biblioteca_api.livro.model.Livro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Column(nullable = false)
    private LocalDate dataReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservaStatus status;

    @Column(nullable = false)
    private int prazoDias;

    public Reserva() {
    }

    public Reserva(Long id, Livro livro, Leitor leitor, LocalDate dataReserva, ReservaStatus status, int prazoDias) {
        this.id = id;
        this.livro = livro;
        this.leitor = leitor;
        this.dataReserva = dataReserva;
        this.status = status;
        this.prazoDias = prazoDias;
    }

    public static Reserva criarAtiva(Livro livro, Leitor leitor, LocalDate dataReserva, int prazoDias) {
        return new Reserva(null, livro, leitor, dataReserva, ReservaStatus.ATIVA, prazoDias);
    }

    public void cancelar() {
        this.status = ReservaStatus.CANCELADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDate dataReserva) {
        this.dataReserva = dataReserva;
    }

    public ReservaStatus getStatus() {
        return status;
    }

    public void setStatus(ReservaStatus status) {
        this.status = status;
    }

    public int getPrazoDias() {
        return prazoDias;
    }

    public void setPrazoDias(int prazoDias) {
        this.prazoDias = prazoDias;
    }
}
