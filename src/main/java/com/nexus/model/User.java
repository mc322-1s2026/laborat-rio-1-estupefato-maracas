package com.nexus.model;

import java.util.List;
import java.util.regex.Pattern;

public class User {
    private static Pattern emailPattern = Pattern.compile("[\\w.-]+@([\\w-]+\\.)+[\\w-]+");
    
    private final String username;
    private final String email;

    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        username = username.trim();
        email = email.trim();

        if (isEmailValid(email)) {
            throw new IllegalArgumentException("Email inválido.");
        }
        this.username = username;
        this.email = email;
    }

    private boolean isEmailValid(String email) {
        return !emailPattern.matcher(email).matches();
    }

    @Override
    public String toString() {
        return getUsername();
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public long getWorkload(List<Task> tasks) {
        return tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS && t.getOwner().getUsername().equals(username))
            .count();
    }
}