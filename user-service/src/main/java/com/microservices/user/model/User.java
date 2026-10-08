package com.microservices.user.model;

import jakarta.persistence.*;

/*
 * User JPA entity — replaces the Phase-3 in-memory record.
 *
 * Database-per-Service: this table lives only in userdb.
 * Password is stored as a BCrypt hash — never plaintext.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected User() {
        // JPA
    }

    public User(String id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public Role getRole()       { return role; }

    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role)           { this.role = role; }
}
