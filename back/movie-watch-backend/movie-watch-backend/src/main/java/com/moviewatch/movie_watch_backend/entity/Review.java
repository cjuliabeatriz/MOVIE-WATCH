
package com.moviewatch.movie_watch_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

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
    // Preenchida automaticamente pelo banco.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Data da última atualização.
    // O trigger do PostgreSQL é responsável por atualizar esse campo.
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    // Construtor vazio exigido pelo JPA.
    public Review() {
    }


    // GETTERS E SETTERS

    public Long getId() {
        return id;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
