
package com.moviewatch.movie_watch_backend.entity;

// Importações do JPA.
// Essas ferramentas permitem mapear a classe Java para uma tabela do PostgreSQL.
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

// Importa o tipo utilizado para armazenar datas e horários.
import java.time.LocalDateTime;

// @Entity informa ao JPA que esta classe representa uma entidade do banco.
@Entity

// @Table define o nome da tabela que esta classe representa no PostgreSQL.
@Table(name = "users")
public class User {

    // @Id indica que este atributo é a chave primária (Primary Key).
    @Id

    // Informa que o próprio PostgreSQL será responsável por gerar o ID.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column configura como este atributo será mapeado para uma coluna.
    // name: nome da coluna no banco.
    // nullable = false: não permite valores nulos.
    // length = 100: limita o tamanho a 100 caracteres.
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Armazena o e-mail do usuário.
    // unique = true: impede que duas contas utilizem o mesmo e-mail.
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // Armazena o hash da senha.
    // Importante: não armazenamos a senha original do usuário.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Indica se a conta está bloqueada.
    // O valor inicial será false, ou seja, a conta começa desbloqueada.
    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    // Armazena o hash do token utilizado para recuperar a senha.
    // Pode ficar vazio (null) quando não existe recuperação em andamento.
    @Column(name = "reset_token_hash", length = 255)
    private String resetTokenHash;

    // Armazena a data e o horário de validade do token de recuperação.
    private LocalDateTime resetTokenExpires;

    // Registra quando a conta foi criada.
    // LocalDateTime permite armazenar data e horário.
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Registra quando as informações da conta foram atualizadas.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // CONSTRUTOR VAZIO
    // O JPA precisa de um construtor sem parâmetros para instanciar a entidade.
    public User() {
    }


    // GETTERS E SETTERS
    // Getters permitem consultar os valores dos atributos.
    // Setters permitem modificar os valores dos atributos.

    // Retorna o ID do usuário.
    public Long getId() {
        return id;
    }

    // Define o ID do usuário.
    public void setId(Long id) {
        this.id = id;
    }

    // Retorna o nome do usuário.
    public String getName() {
        return name;
    }

    // Altera o nome do usuário.
    public void setName(String name) {
        this.name = name;
    }

    // Retorna o e-mail do usuário.
    public String getEmail() {
        return email;
    }

    // Altera o e-mail do usuário.
    public void setEmail(String email) {
        this.email = email;
    }

    // Retorna o hash da senha.
    public String getPasswordHash() {
        return passwordHash;
    }

    // Define o hash da senha.
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Retorna se a conta está bloqueada ou não.
    public Boolean getIsBlocked() {
        return isBlocked;
    }

    // Altera o estado de bloqueio da conta.
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    // Retorna o hash do token de recuperação.
    public String getResetTokenHash() {
        return resetTokenHash;
    }

    // Define o hash do token de recuperação.
    public void setResetTokenHash(String resetTokenHash) {
        this.resetTokenHash = resetTokenHash;
    }

    // Retorna a data de validade do token.
    public LocalDateTime getResetTokenExpires() {
        return resetTokenExpires;
    }

    // Define a data de validade do token.
    public void setResetTokenExpires(LocalDateTime resetTokenExpires) {
        this.resetTokenExpires = resetTokenExpires;
    }

    // Retorna a data de criação da conta.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Define a data de criação da conta.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Retorna a data da última atualização.
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Define a data da última atualização.
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
