package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Account.findAll", query = "FROM Account")
@NamedQuery(name = "Account.findByUsername", query = "FROM Account WHERE username = :username")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String hashedPassword;
    @Column(nullable = false)
    private String salt;
    @Column(nullable = false)
    private String role;
}
