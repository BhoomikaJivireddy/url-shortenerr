package com.susu.urlshortener.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;
    @Setter
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(nullable = false)
    private String role;

    public User() {
    }

    public User(
            String name,
            String email,
            String password,
            String role
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}