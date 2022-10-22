package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
    @Column(nullable = false)
    private Date createdAt;

    @OneToMany(mappedBy = "teacher")
    private List<ClassTeacher> classrooms;
    @OneToMany(mappedBy = "teacher")
    private List<RecordEntry> recordEntries;
    @OneToMany(mappedBy = "requester")
    private List<RecordEntry> requestedRecordEntries;
    @OneToMany(mappedBy = "approver")
    private List<RecordEntry> approvedRecordEntries;
    @OneToMany(mappedBy = "teacher")
    private List<PendingRecordEntry> pendingRecordEntries;
    @OneToMany(mappedBy = "requester")
    private List<PendingRecordEntry> requestedPendingRecordEntries;
    @OneToMany(mappedBy = "homeroomTeacher")
    private List<Classroom> homeClassrooms;
}
