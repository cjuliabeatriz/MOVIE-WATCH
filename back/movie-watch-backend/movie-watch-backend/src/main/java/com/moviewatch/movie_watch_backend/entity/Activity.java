
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

// Informa ao JPA que esta classe representa uma entidade.
@Entity

// Relaciona a classe com a tabela activities.
@Table(name = "activities")
public class Activity {

    // Chave primária da tabela.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com o usuário responsável pela atividade.
    // Um usuário pode possuir várias atividades.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Identifica qual ação foi realizada.
    // Exemplos: FAVORITE_ADDED ou REVIEW_CREATED.
    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    // Identificador do filme relacionado à atividade.
    // Pode ser nulo quando a atividade não estiver ligada a um filme.
    @Column(name = "movie_id", length = 255)
    private String movieId;

    // Data e horário em que a atividade aconteceu.
    // O PostgreSQL preenche esse campo automaticamente.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;


    // Construtor vazio exigido pelo JPA.
    public Activity() {
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

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
