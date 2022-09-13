package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Account.findAll", query = "FROM Account")
@NamedQuery(name = "Account.findByUsername", query = "FROM Account WHERE username = :username")
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

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Student student;
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Profile teacher;

    @OneToMany(mappedBy = "teacher")
    private Set<ClassTeacher> classrooms;
    @OneToMany(mappedBy = "teacher")
    private Set<RecordEntry> recordEntries;
    @OneToMany(mappedBy = "student")
    private Set<Record> records;
    @OneToMany(mappedBy = "teacher")
    private Set<PendingRecordEntry> pendingRecordEntries;
}
