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
public class Student implements Serializable {
    @Id
    @OneToOne
    @JoinColumn
    private Account account;
    @Column(nullable = false)
    private String ethnic;
    @Column(nullable = false)
    private String fatherName;
    @Column(nullable = false)
    private String fatherJob;
    @Column(nullable = false)
    private String motherName;
    @Column(nullable = false)
    private String motherJob;
    @Column(nullable = false)
    private String homeTown;

    @OneToMany(mappedBy = "student")
    private Set<ClassStudent> classrooms;
}
