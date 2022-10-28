package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Profile implements Serializable {
    @Id
    private long id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    private Account account;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private boolean male;
    @Column(nullable = false)
    private String avatar;
    @Column(nullable = false)
    private Date birthDate;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private boolean updated;

    public static Profile getOrDefault(Session session, long id) {
        Profile profile = session.get(Profile.class, id);
        if (profile == null) {
            profile = new Profile();
            profile.setId(id);
            profile.setFirstName("");
            profile.setLastName("");
            profile.setMale(true);
            profile.setAvatar("");
            profile.setBirthDate(new Date());
            profile.setAddress("");
            profile.setPhone("");
            profile.setEmail("");
            profile.setUpdated(false);
        }
        return profile;
    }
}
