
package com.moviewatch.movie_watch_backend.repository;

// Importa a entidade Review, que representa a tabela "reviews".
import com.moviewatch.movie_watch_backend.entity.Review;

// Importa o JpaRepository do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa EntityGraph para carregar o usuário junto com a avaliação.
import org.springframework.data.jpa.repository.EntityGraph;

// Importa List e Optional.
import java.util.List;
import java.util.Optional;

/*
 * Repository responsável pelo acesso às avaliações e comentários.
 *
 * JpaRepository recebe:
 * Review -> entidade que será gerenciada.
 * Long   -> tipo do ID da entidade Review.
 *
 * O Spring Data JPA fornece automaticamente as operações
 * básicas de acesso ao banco de dados.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Métodos básicos disponibilizados:
    //
    // save()       -> cadastrar ou atualizar uma avaliação.
    // findById()   -> buscar uma avaliação pelo ID.
    // findAll()    -> listar todas as avaliações.
    // deleteById() -> excluir uma avaliação pelo ID.

    // Lista as avaliações de um filme, da mais recente para a mais antiga.
    // @EntityGraph carrega o usuário junto, evitando consultas N+1.
    @EntityGraph(attributePaths = "user")
    List<Review> findByMovieIdOrderByCreatedAtDesc(String movieId);

    // Lista as avaliações feitas por um usuário.
    @EntityGraph(attributePaths = "user")
    List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Busca a avaliação de um usuário para um filme específico.
    Optional<Review> findByUser_IdAndMovieId(Long userId, String movieId);

    // Verifica se o usuário já avaliou aquele filme.
    boolean existsByUser_IdAndMovieId(Long userId, String movieId);
}
