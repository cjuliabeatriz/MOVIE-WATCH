
package com.moviewatch.movie_watch_backend.repository;

// Importa a entidade Activity, que representa a tabela "activities".
import com.moviewatch.movie_watch_backend.entity.Activity;

// Importa o JpaRepository do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa List para trabalhar com listas de atividades.
import java.util.List;

/*
 * Repository responsável pelo acesso ao histórico de atividades.
 *
 * JpaRepository recebe:
 * Activity -> entidade que será gerenciada.
 * Long     -> tipo do ID da entidade Activity.
 *
 * O Spring Data JPA cria a implementação automaticamente.
 */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Métodos básicos disponibilizados:
    //
    // save()       -> registrar uma atividade.
    // findById()   -> buscar uma atividade pelo ID.
    // findAll()    -> listar todas as atividades.
    // deleteById() -> excluir uma atividade pelo ID.

    // Lista as atividades de um usuário em ordem cronológica decrescente.
    List<Activity> findByUser_IdOrderByCreatedAtDesc(Long userId);
}

