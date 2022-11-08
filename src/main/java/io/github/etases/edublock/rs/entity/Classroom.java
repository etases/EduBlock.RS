package io.github.etases.edublock.rs.entity;

import io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

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
    @GenericGenerator(name = "ExistOrGenerate", strategy = UseExistOrIncrementGenerator.CLASS_PATH)
    @GeneratedValue(generator = "ExistOrGenerate")
    @Column(unique = true, nullable = false)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int grade;
    @Column(nullable = false, name = "start_year")
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
