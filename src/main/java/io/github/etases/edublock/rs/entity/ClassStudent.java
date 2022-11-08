package io.github.etases.edublock.rs.entity;

import io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "ClassStudent.findByStudent", query = "FROM ClassStudent WHERE student.id = :studentId")
@NamedQuery(name = "ClassStudent.findByClassroomAndStudent", query = "FROM ClassStudent WHERE classroom.id = :classroomId and student.id = :studentId")
public class ClassStudent implements Serializable {
    @Id
    @GenericGenerator(name = "ExistOrGenerate", strategy = UseExistOrIncrementGenerator.CLASS_PATH)
    @GeneratedValue(generator = "ExistOrGenerate")
    @Column(unique = true, nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Classroom classroom;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Student student;
}
