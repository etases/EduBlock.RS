package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Student.findAll", query = "FROM Student")
public class Student implements Serializable {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
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
    private String guardianName;
    @Column(nullable = false)
    private String guardianJob;
    @Column(nullable = false)
    private String homeTown;

    @OneToMany(mappedBy = "student")
    private List<ClassStudent> classrooms;
    @OneToMany(mappedBy = "student")
    private List<Record> records;
}
