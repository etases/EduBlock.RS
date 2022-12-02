package io.github.etases.edublock.rs.entity;

import io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "RecordEntry.findNeedUpdate", query = "FROM RecordEntry where updateComplete = false")
public class RecordEntry implements Serializable {
    @Id
    @GenericGenerator(name = "ExistOrGenerate", strategy = UseExistOrIncrementGenerator.CLASS_PATH)
    @GeneratedValue(generator = "ExistOrGenerate")
    @Column(unique = true, nullable = false)
    private Long id;
    @Column(nullable = false)
    private long subjectId;

    @Column(nullable = false)
    private float firstHalfOral1;
    @Column(nullable = false)
    private float firstHalfOral2;
    @Column(nullable = false)
    private float firstHalfOral3;
    @Column(nullable = false)
    private float firstHalfMinute1;
    @Column(nullable = false)
    private float firstHalfMinute2;
    @Column(nullable = false)
    private float firstHalfMinute3;
    @Column(nullable = false)
    private float firstHalfSession1;
    @Column(nullable = false)
    private float firstHalfSession2;
    @Column(nullable = false)
    private float firstHalfSession3;
    @Column(nullable = false)
    private float firstHalfSession4;
    @Column(nullable = false)
    private float firstHalfSession5;
    @Column(nullable = false)
    private float firstHalfSession6;
    @Column(nullable = false)
    private float firstHalfFinal;
    @Column(nullable = false)
    private float firstHalfScore;

    @Column(nullable = false)
    private float secondHalfOral1;
    @Column(nullable = false)
    private float secondHalfOral2;
    @Column(nullable = false)
    private float secondHalfOral3;
    @Column(nullable = false)
    private float secondHalfMinute1;
    @Column(nullable = false)
    private float secondHalfMinute2;
    @Column(nullable = false)
    private float secondHalfMinute3;
    @Column(nullable = false)
    private float secondHalfSession1;
    @Column(nullable = false)
    private float secondHalfSession2;
    @Column(nullable = false)
    private float secondHalfSession3;
    @Column(nullable = false)
    private float secondHalfSession4;
    @Column(nullable = false)
    private float secondHalfSession5;
    @Column(nullable = false)
    private float secondHalfSession6;
    @Column(nullable = false)
    private float secondHalfFinal;
    @Column(nullable = false)
    private float secondHalfScore;

    @Column(nullable = false)
    private float finalScore;

    @Column(nullable = false)
    private Date requestDate;
    @Column(nullable = false)
    private Date approvalDate;
    @Column(nullable = false)
    private boolean updateComplete;
    @ManyToOne
    @JoinColumn
    private Account teacher;
    @ManyToOne
    @JoinColumn
    private Account requester;
    @ManyToOne
    @JoinColumn
    private Account approver;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Record record;
}
