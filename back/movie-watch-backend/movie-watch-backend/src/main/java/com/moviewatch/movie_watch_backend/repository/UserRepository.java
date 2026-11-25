package com.moviewatch.movie_watch_backend.repository;

//importa o user.
import  com.moviewatch.movie_watch_backend.entity.User;

// Importa a interface JpaRepository do Spring Data JPA.
// Ela disponibiliza métodos prontos para operações no banco de dados.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa Optional para representar resultados que podem não existir.
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Não precisamos implementar os métodos básicos manualmente.
    // O JpaRepository já fornece operações como:
    // save()       -> salvar ou atualizar um usuário.
    // findById()   -> buscar usuário pelo ID.
    // findAll()    -> listar todos os usuários.
    // deleteById() -> excluir usuário pelo ID.
    // existsById() -> verificar se um usuário existe.

    // Busca um usuário pelo e-mail.
    // O e-mail é normalizado (minúsculas) antes da consulta.
    Optional<User> findByEmail(String email);

    // Verifica se já existe uma conta com o e-mail informado.
    // Usado para impedir cadastro duplicado.
    boolean existsByEmail(String email);

    // Busca um usuário pelo hash do token de recuperação.
    // O token em si nunca é armazenado; apenas o hash SHA-256.
    Optional<User> findByResetTokenHash(String resetTokenHash);
}