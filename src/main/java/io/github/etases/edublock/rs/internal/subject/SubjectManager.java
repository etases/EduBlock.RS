package io.github.etases.edublock.rs.internal.subject;

import lombok.experimental.UtilityClass;
import me.hsgamer.hscore.config.Config;
import me.hsgamer.hscore.config.configurate.ConfigurateConfig;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class SubjectManager {
    private static final Map<Long, Subject> subjects = new HashMap<>();

    static {
        File subjectFile;
        try (var stream = SubjectManager.class.getClassLoader().getResourceAsStream("subjects.yml")) {
            subjectFile = File.createTempFile("subject", ".yml");
            subjectFile.deleteOnExit();
            Files.copy(Objects.requireNonNull(stream), subjectFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Config config = new ConfigurateConfig(subjectFile, YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK));
        config.setup();

        if (config.contains("subject") && config.getNormalized("subject") instanceof List<?> rawList) {
            for (Object object : rawList) {
                if (object instanceof Map<?, ?> rawMap) {
                    Map<String, Object> map = new HashMap<>();
                    rawMap.forEach((k, v) -> map.put(Objects.toString(k), v));
                    var subject = Subject.fromMap(map);
                    subjects.put(subject.getId(), subject);
                }
            }
        }
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
