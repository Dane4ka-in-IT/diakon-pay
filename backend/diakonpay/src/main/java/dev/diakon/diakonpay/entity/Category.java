package dev.diakon.diakonpay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @Column(name = "uuid", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 50)
    @NotNull
    @Column(name = "name_category", nullable = false, length = 50)
    private String nameCategory;

    @Column(name = "category_description", length = Integer.MAX_VALUE)
    private String categoryDescription;

    @Size(max = 7)
    @NotNull
    @Column(name = "type", nullable = false, length = 7)
    private String type;

}