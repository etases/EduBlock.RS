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
@NamedQuery(name = "Classroom.findByName", query = "FROM Classroom WHERE name = :name")
public class Classroom implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int grade;

    @OneToMany(mappedBy = "classroom")
    private Set<ClassStudent> students;
    @OneToMany(mappedBy = "classroom")
    private Set<ClassTeacher> teachers;
    @OneToMany(mappedBy = "classroom")
    private Set<Record> records;
}
