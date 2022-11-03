package io.github.etases.edublock.rs.internal.classification;

import io.github.etases.edublock.rs.internal.subject.Subject;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import lombok.experimental.UtilityClass;
import me.hsgamer.hscore.config.Config;
import me.hsgamer.hscore.config.simpleconfiguration.SimpleConfig;
import org.simpleyaml.configuration.file.YamlFile;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@UtilityClass
public class ClassificationManager {
    private static final List<Classification> classifications = new ArrayList<>();

    static {
        File classificationFile = new File("classifications.yml");

        if (!classificationFile.exists()) {
            try (var stream = SubjectManager.class.getClassLoader().getResourceAsStream("classifications.yml")) {
                if (classificationFile.createNewFile()) {
                    Files.copy(Objects.requireNonNull(stream), classificationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        Config config = new SimpleConfig<>(classificationFile, new YamlFile(), (file, yamlFile) -> {
            yamlFile.setConfigurationFile(file);
            try {
                yamlFile.loadWithComments();
            } catch (IOException e) {
                Logger.warn(e);
            }
        });

        if (config.contains("classification")) {
            config.getValues("classification", false).forEach((key, value) -> {
                if (value instanceof Map<?, ?> rawMap) {
                    Map<String, Object> map = new HashMap<>();
                    rawMap.forEach((k, v) -> map.put(Objects.toString(k), v));
                    var classification = Classification.fromMap(key, map);
                    classifications.add(classification);
                }
            });
        }
    }

    public List<Classification> getClassifications() {
        return Collections.unmodifiableList(classifications);
    }

    public Classification getClassification(String identifier) {
        return classifications.stream().filter(classification -> classification.getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
    }

    public Classification classify(Map<Subject, Float> subjectScoreMap) {
        return classifications.stream().filter(classification -> classification.isApplicable(subjectScoreMap)).min(Comparator.comparingInt(Classification::getLevel)).orElse(null);
    }
}
