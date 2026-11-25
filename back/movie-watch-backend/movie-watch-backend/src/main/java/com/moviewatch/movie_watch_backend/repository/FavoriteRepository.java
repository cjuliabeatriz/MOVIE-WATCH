
package com.moviewatch.movie_watch_backend.repository;

// Importa a entidade Favorite, que representa a tabela "favorites".
import com.moviewatch.movie_watch_backend.entity.Favorite;

// Importa o JpaRepository do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa List e Optional.
import java.util.List;
import java.util.Optional;

/*
 * Repository responsável pelo acesso aos filmes favoritos.
 *
 * JpaRepository recebe:
 * Favorite -> entidade que será gerenciada.
 * Long     -> tipo do ID da entidade Favorite.
 *
 * O Spring Data JPA cria automaticamente a implementação
 * desta interface durante a execução da aplicação.
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Métodos básicos já disponibilizados pelo JpaRepository:
    //
    // save()       -> cadastrar ou atualizar um favorito.
    // findById()   -> buscar um favorito pelo ID.
    // findAll()    -> listar todos os favoritos.
    // deleteById() -> excluir um favorito pelo ID.

    // Lista os favoritos de um usuário, do mais recente para o mais antigo.
    List<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Busca um favorito específico de um usuário para um filme.
    Optional<Favorite> findByUser_IdAndMovieId(Long userId, String movieId);

    // Verifica se o filme já está favoritado por aquele usuário.
    boolean existsByUser_IdAndMovieId(Long userId, String movieId);

    // Conta quantos favoritos o usuário possui.
    long countByUser_Id(Long userId);
}
