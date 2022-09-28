package io.github.etases.edublock.rs.model.output;
import java.util.Date;

public record ProfileOutput (
     long id,

     String firstName,

     String lastName,

     String avatar,

     Date birthDate,

     String address,

     String phone,

     String email

)
{}
