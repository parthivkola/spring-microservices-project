package com.microservices.user.model;

/*
 * Role — used for Role-Based Access Control (RBAC).
 *
 * Spring Security expects authorities as "ROLE_ADMIN", "ROLE_USER".
 * JwtService embeds the role name (without ROLE_ prefix) in the JWT claim;
 * SecurityConfig maps it to GrantedAuthority with ROLE_ prefix.
 *
 * Interview tip:
 *   Authentication = who you are (JWT proves identity)
 *   Authorization  = what you can do (roles / permissions)
 */
public enum Role {
    USER,
    ADMIN
}
