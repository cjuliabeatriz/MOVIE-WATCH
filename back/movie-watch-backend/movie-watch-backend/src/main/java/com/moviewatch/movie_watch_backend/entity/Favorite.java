
package com.moviewatch.movie_watch_backend.entity;

// Importações necessárias para mapear a classe com o PostgreSQL.
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

// Informa ao JPA que esta classe representa uma tabela.
@Entity

// Define o nome da tabela no PostgreSQL.
@Table(name = "favorites")
public class Favorite {

    // Chave primária da tabela.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com User.
    // Muitos favoritos podem pertencer ao mesmo usuário.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    // user_id é a chave estrangeira que aponta para users.id.
    private User user;

    // Identificador do filme fornecido pelo Cinemeta.
    // Não precisamos armazenar os dados completos do filme no banco.
    @Column(name = "movie_id", nullable = false, length = 255)
    private String movieId;

    // Data em que o filme foi favoritado.
    // O PostgreSQL preenche esse campo automaticamente.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;


    // Construtor vazio exigido pelo JPA.
    public Favorite() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
