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
@NamedQuery(name = "Subject.findAll", query = "FROM Subject")
public class Subject implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "subject")
    private List<ClassTeacher> teachers;
    @OneToMany(mappedBy = "subject")
    private List<RecordEntry> records;
    @OneToMany(mappedBy = "subject")
    private List<PendingRecordEntry> pendingRecords;
}
