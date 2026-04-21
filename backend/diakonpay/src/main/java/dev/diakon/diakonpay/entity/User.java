package dev.diakon.diakonpay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @NotNull
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 254)
    @NotNull
    @Column(name = "email", nullable = false, length = 254)
    private String userEmail;

    @NotNull
    @Column(name = "password", nullable = false, length = Integer.MAX_VALUE)
    private String userPassword;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

}