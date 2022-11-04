package io.github.etases.edublock.rs.internal.classification;

import io.github.etases.edublock.rs.entity.Record;
import io.github.etases.edublock.rs.entity.RecordEntry;
import io.github.etases.edublock.rs.internal.subject.Subject;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import io.github.etases.edublock.rs.model.output.element.ClassificationReportOutput;
import io.github.etases.edublock.rs.model.output.element.RecordEntryOutput;
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
        File classificationFile;
        try (var stream = SubjectManager.class.getClassLoader().getResourceAsStream("classifications.yml")) {
            classificationFile = File.createTempFile("classification", ".yml");
            Files.copy(Objects.requireNonNull(stream), classificationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Config config = new SimpleConfig<>(classificationFile, new YamlFile(), (file, yamlFile) -> {
            yamlFile.setConfigurationFile(file);
            try {
                yamlFile.loadWithComments();
            } catch (IOException e) {
                Logger.warn(e);
            }
        });
        config.setup();

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
        return classifications.stream()
                .filter(classification -> classification.getIdentifier().equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(new Classification(identifier, identifier, Collections.emptyList(), -1, Collections.emptyList()));
    }

    public Classification classify(Map<Subject, Float> subjectScoreMap) {
        return classifications.stream()
                .filter(classification -> classification.isApplicable(subjectScoreMap))
                .min(Comparator.comparingInt(Classification::getLevel))
                .orElseGet(Classification::new);
    }

    public Classification classifyRawSubjectMap(Map<Long, Float> subjectScoreMap) {
        Map<Subject, Float> subjectFloatMap = new HashMap<>();
        subjectScoreMap.forEach((id, score) -> {
            Subject subject = SubjectManager.getSubject(id);
            if (subject != null) {
                subjectFloatMap.put(subject, score);
            }
        });
        return classify(subjectFloatMap);
    }

    public ClassificationReport createReport(Record record) {
        Map<Long, RecordEntry> rawSubjectEntryMap = new HashMap<>();
        record.getRecordEntry().forEach(recordEntry -> {
            var subjectId = recordEntry.getSubjectId();
            rawSubjectEntryMap.merge(subjectId, recordEntry, (oldEntry, newEntry) -> {
                if (oldEntry.getApprovalDate() == null) {
                    return newEntry;
                } else if (newEntry.getApprovalDate() == null) {
                    return oldEntry;
                } else {
                    return oldEntry.getApprovalDate().compareTo(newEntry.getApprovalDate()) > 0 ? oldEntry : newEntry;
                }
            });
        });

        Map<Subject, RecordEntry> subjectEntryMap = new HashMap<>();
        rawSubjectEntryMap.forEach((subjectId, recordEntry) -> {
            var subject = SubjectManager.getSubject(subjectId);
            if (subject != null) {
                subjectEntryMap.put(subject, recordEntry);
            }
        });

        Map<Subject, Float> subjectFirstHalfScoreMap = new HashMap<>();
        Map<Subject, Float> subjectSecondHalfScoreMap = new HashMap<>();
        Map<Subject, Float> subjectFinalScoreMap = new HashMap<>();
        subjectEntryMap.forEach((subject, recordEntry) -> {
            subjectFirstHalfScoreMap.put(subject, recordEntry.getFirstHalfScore());
            subjectSecondHalfScoreMap.put(subject, recordEntry.getSecondHalfScore());
            subjectFinalScoreMap.put(subject, recordEntry.getFinalScore());
        });

        Classification firstHalfClassify = classify(subjectFirstHalfScoreMap);
        Classification secondHalfClassify = classify(subjectSecondHalfScoreMap);
        Classification finalClassify = classify(subjectFinalScoreMap);

        return new ClassificationReport(firstHalfClassify, secondHalfClassify, finalClassify);
    }

    public ClassificationReportOutput createReport(List<RecordEntryOutput> entryOutputs) {
        Map<Subject, Float> subjectFirstHalfScoreMap = new HashMap<>();
        Map<Subject, Float> subjectSecondHalfScoreMap = new HashMap<>();
        Map<Subject, Float> subjectFinalScoreMap = new HashMap<>();
        entryOutputs.forEach(recordEntry -> {
            var subject = SubjectManager.getSubject(recordEntry.getSubjectId());
            if (subject != null) {
                subjectFirstHalfScoreMap.put(subject, recordEntry.getFirstHalfScore());
                subjectSecondHalfScoreMap.put(subject, recordEntry.getSecondHalfScore());
                subjectFinalScoreMap.put(subject, recordEntry.getFinalScore());
            }
        });

        Classification firstHalfClassify = classify(subjectFirstHalfScoreMap);
        Classification secondHalfClassify = classify(subjectSecondHalfScoreMap);
        Classification finalClassify = classify(subjectFinalScoreMap);
        ClassificationReport report = new ClassificationReport(firstHalfClassify, secondHalfClassify, finalClassify);

        return ClassificationReportOutput.fromInternal(report);
    }
}
