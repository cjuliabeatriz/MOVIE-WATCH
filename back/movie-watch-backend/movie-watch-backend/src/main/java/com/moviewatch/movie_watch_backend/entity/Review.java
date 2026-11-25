
package com.moviewatch.movie_watch_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

// Representa uma entidade do banco de dados.
@Entity

// Mapeia esta classe para a tabela reviews.
@Table(name = "reviews")
public class Review {

    // Identificador único da avaliação.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com o usuário que escreveu a avaliação.
    // Um usuário pode escrever várias avaliações.
    // @JsonIgnore evita que a entidade dona apareça na serialização.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Identificador externo do filme no Cinemeta.
    @Column(name = "movie_id", nullable = false, length = 255)
    private String movieId;

    // Nota atribuída ao filme.
    // Short corresponde ao tipo SMALLINT do PostgreSQL.
    // Pode ser nula, conforme a regra definida no nosso SQL.
    @Column(name = "rating")
    private Short rating;

    // Comentário escrito pelo usuário.
    // columnDefinition = TEXT corresponde ao tipo TEXT do PostgreSQL.
    private String comment;

    // Data de criação da avaliação.
    // A aplicação preenche o valor; o banco também possui DEFAULT.
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Data da última atualização.
    // O trigger do PostgreSQL também atualiza este campo.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // Construtor vazio exigido pelo JPA.
    public Review() {
    }

    // Preenche as datas antes da inserção.
    @PrePersist
    void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = agora;
        }

        if (updatedAt == null) {
            updatedAt = agora;
        }
    }

    // Mantém a data de atualização coerente antes de cada UPDATE.
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


    // GETTERS E SETTERS

    public Long getId() {
        return id;
    }

    // Define o ID (usado pelo JPA e em testes).
    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public Short getRating() {
        return rating;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Define a data de criação (usada em testes).
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Define a data de atualização (usada em testes).
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
