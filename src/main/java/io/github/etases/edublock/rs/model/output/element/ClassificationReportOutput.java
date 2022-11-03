package io.github.etases.edublock.rs.model.output.element;

import io.github.etases.edublock.rs.internal.classification.ClassificationManager;
import io.github.etases.edublock.rs.internal.classification.ClassificationReport;
import io.github.etases.edublock.rs.model.fabric.Classification;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationReportOutput {
    ClassificationOutput firstHalfClassify = new ClassificationOutput();
    ClassificationOutput secondHalfClassify = new ClassificationOutput();
    ClassificationOutput finalClassify = new ClassificationOutput();

    public static ClassificationReportOutput fromInternal(ClassificationReport classificationReport) {
        return new ClassificationReportOutput(
                ClassificationOutput.fromInternal(classificationReport.getFirstHalfClassify()),
                ClassificationOutput.fromInternal(classificationReport.getSecondHalfClassify()),
                ClassificationOutput.fromInternal(classificationReport.getFinalClassify())
        );
    }

    public static ClassificationReportOutput fromFabricModel(Classification classification) {
        return fromInternal(new ClassificationReport(
                ClassificationManager.getClassification(classification.getFirstHalfClassify()),
                ClassificationManager.getClassification(classification.getSecondHalfClassify()),
                ClassificationManager.getClassification(classification.getFinalClassify())
        ));
    }
}
