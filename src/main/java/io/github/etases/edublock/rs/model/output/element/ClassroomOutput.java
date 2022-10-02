package io.github.etases.edublock.rs.model.output.element;


import io.github.etases.edublock.rs.entity.Classroom;
import io.github.etases.edublock.rs.entity.Profile;

import java.util.function.LongFunction;

public record ClassroomOutput(
        long id,
        String name,
        int grade,
        AccountWithProfileOutput homeroomTeacher
) {
    public static ClassroomOutput fromEntity(Classroom classroom, LongFunction<Profile> profileFunction) {
        return new ClassroomOutput(
                classroom.getId(),
                classroom.getName(),
                classroom.getGrade(),
                AccountWithProfileOutput.fromEntity(classroom.getHomeroomTeacher(), profileFunction)
        );
    }
}