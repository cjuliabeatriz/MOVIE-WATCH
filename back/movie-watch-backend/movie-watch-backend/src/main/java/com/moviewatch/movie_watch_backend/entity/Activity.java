
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
    // @JsonIgnore evita que a entidade dona apareça na serialização.
    @JsonIgnore
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
    // A aplicação preenche o valor; o banco também possui DEFAULT.
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // Construtor vazio exigido pelo JPA.
    public Activity() {
    }

    // Garante que a data da atividade seja preenchida na inserção.
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    // Define a data de criação (usada em testes).
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
