package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Date requestDate;
    @Column(nullable = false)
    private Date approvalDate;
    @Column(nullable = false)
    private boolean updateComplete;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account teacher;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account requester;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account approver;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Record record;
}
