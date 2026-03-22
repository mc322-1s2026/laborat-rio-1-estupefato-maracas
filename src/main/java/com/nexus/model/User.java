package com.nexus.model;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Representa um usuário no sistema Nexus.
 * <p>Um usuário possui um nome de usuário únido e um email válido.
 * Fornece métodos para calcular a carga de trabalho atual e obter informações.
 */
public class User {
    /**
     * Padrão regex para validação de endereços de email.
     */
    private static Pattern emailPattern = Pattern.compile("[\\w.-]+@([\\w-]+\\.)+[\\w-]+");
    
    /**
     * Nome de usuário único no sistema.
     */
    private final String username;
    /**
     * Endereço de email do usuário, validado durante construção.
     */
    private final String email;

    /**
     * Constrói um novo usuário com o nome de usuário e email fornecidos.
     *
     * @param username o nome de usuário; não pode ser nulo ou vazio
     * @param email o endereço de email; deve ser um email válido
     * @throws IllegalArgumentException se o username for vazio ou o email for inválido
     */
    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        username = username.trim();
        email = email.trim();

        if (!isEmailValid(email)) {
            throw new IllegalArgumentException("Email inválido.");
        }
        this.username = username;
        this.email = email;
    }

    /**
     * Valida um endereço de email usando o padrão regex definido.
     *
     * @param email o email a ser validado
     * @return {@code true} se o email é válido, {@code false} se <b>não</b> é válido
     */
    private boolean isEmailValid(String email) {
        return emailPattern.matcher(email).matches();
    }

    /**
     * Retorna a representação em string do usuário.
     *
     * @return o nome de usuário
     */
    @Override
    public String toString() {
        return getUsername();
    }

    /**
     * Retorna o email do usuário.
     *
     * @return o endereço de email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retorna o nome de usuário do usuário.
     *
     * @return o nome de usuário
     */
    public String getUsername() {
        return username;
    }

    /**
     * Calcula a carga de trabalho atual do usuário, contando quantas
     * {@link Task} em estado {@link TaskStatus#IN_PROGRESS} lhe são atribuídas.
     *
     * @param tasks a lista de todas as tarefas do sistema
     * @return o número de tarefas em progresso atribuídas a este usuário
     */
    public long calculateWorkload(List<Task> tasks) {
        return tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS && t.getOwner().getUsername().equals(username))
            .count();
    }
}