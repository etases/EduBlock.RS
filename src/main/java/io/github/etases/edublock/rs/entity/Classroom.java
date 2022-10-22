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
@NamedQuery(name = "Classroom.findAll", query = "FROM Classroom")
@NamedQuery(name = "Classroom.findByName", query = "FROM Classroom WHERE name = :name")
@NamedQuery(name = "Classroom.findByHomeroomTeacher", query = "FROM Classroom WHERE homeroomTeacher.id = :teacherId")
public class Classroom implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int grade;
    @Column(nullable = false)
    private int year;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account homeroomTeacher;

    @OneToMany(mappedBy = "classroom")
    private List<ClassStudent> students;
    @OneToMany(mappedBy = "classroom")
    private List<ClassTeacher> teachers;
    @OneToMany(mappedBy = "classroom")
    private List<Record> records;
}
