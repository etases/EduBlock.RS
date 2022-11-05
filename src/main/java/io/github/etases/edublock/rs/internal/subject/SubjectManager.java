package io.github.etases.edublock.rs.internal.subject;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class SubjectManager {
    private static final Map<Long, Subject> subjects;
    private static final Object lock = new Object();

    static {
        synchronized (lock) {
            subjects = new HashMap<>();
            try (
                    var stream = SubjectManager.class.getClassLoader().getResourceAsStream("subjects.csv");
                    var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream)))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    var subject = parseSubject(line);
                    subjects.put(subject.getId(), subject);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static Subject parseSubject(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        var parts = line.split(",");
        if (parts.length < 3) {
            return null;
        }
        var subject = new Subject();
        subject.setId(Long.parseLong(parts[0].trim()));
        subject.setIdentifier(parts[1].trim());
        subject.setName(parts[2].trim());
        subject.setOtherNames(List.of(parts).subList(3, parts.length).stream().map(String::trim).toList());
        return subject;
    }

    public static Subject getSubject(long id) {
        return subjects.get(id);
    }

    public static Subject getSubject(String name) {
        return subjects.values().stream()
                .filter(subject -> subject.getIdentifier().equalsIgnoreCase(name) || subject.getName().equalsIgnoreCase(name) || subject.getOtherNames().stream().anyMatch(name::equalsIgnoreCase))
                .findFirst()
                .orElse(null);
    }

    public static List<Subject> getSubjects() {
        return List.copyOf(subjects.values());
    }
}
