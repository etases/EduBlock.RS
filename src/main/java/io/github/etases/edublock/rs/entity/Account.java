package io.github.etases.edublock.rs.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Account.findAll", query = "FROM Account")
@NamedQuery(name = "Account.findByUsername", query = "FROM Account WHERE username = :username")
@NamedQuery(name = "Account.countByUsernameRegex", query = "select count(a) FROM Account a WHERE username LIKE :username")
@NamedQuery(name = "Account.findByRole", query = "FROM Account WHERE role = :role")
public class Account implements Serializable {
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

    @OneToMany(mappedBy = "teacher")
    private Set<ClassTeacher> classrooms;
    @OneToMany(mappedBy = "teacher")
    private Set<RecordEntry> recordEntries;
    @OneToMany(mappedBy = "teacher")
    private Set<PendingRecordEntry> pendingRecordEntries;
}
