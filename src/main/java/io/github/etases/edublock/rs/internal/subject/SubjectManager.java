package io.github.etases.edublock.rs.internal.subject;

import lombok.experimental.UtilityClass;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
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
            var subjectFile = new File("subjects.csv");

            boolean canLoad = true;
            if (!subjectFile.exists()) {
                try (var stream = SubjectManager.class.getClassLoader().getResourceAsStream("subjects.csv")) {
                    if (subjectFile.createNewFile()) {
                        Files.copy(Objects.requireNonNull(stream), subjectFile.toPath());
                    }
                } catch (Exception e) {
                    Logger.error(e, "Error while creating subjects.csv");
                    canLoad = false;
                }
            }

            if (canLoad) {
                try (var reader = new BufferedReader(new FileReader(subjectFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        var subject = parseSubject(line);
                        subjects.put(subject.getId(), subject);
                    }
                } catch (Exception e) {
                    Logger.error(e, "Error while loading subjects.csv");
                }
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
