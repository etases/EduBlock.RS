package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecordEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false)
    private long subjectId;
    @Column(nullable = false)
    private float firstHalfScore;
    @Column(nullable = false)
    private float secondHalfScore;
    @Column(nullable = false)
    private float finalScore;
    @Column(nullable = false)
    private long teacherId;
}
