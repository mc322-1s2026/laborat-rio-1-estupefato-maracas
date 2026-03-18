package com.nexus.model;

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

    public String consultEmail() {
        return email;
    }

    public String consultUsername() {
        return username;
    }

    public long calculateWorkload() {
        return 0; 
    }
}