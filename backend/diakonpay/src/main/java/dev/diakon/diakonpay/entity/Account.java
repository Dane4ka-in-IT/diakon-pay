package dev.diakon.diakonpay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "uuid", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 50)
    @NotNull
    @Column(name = "bank_name", nullable = false, length = 50)
    private String bankName;

    @Size(max = 20)
    @NotNull
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @NotNull
    @Column(name = "balance", nullable = false, precision = 20, scale = 4)
    private BigDecimal balance;

    @Size(max = 3)
    @NotNull
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @ColumnDefault("false")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}