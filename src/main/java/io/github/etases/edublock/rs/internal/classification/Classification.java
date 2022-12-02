package io.github.etases.edublock.rs.internal.classification;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import io.github.etases.edublock.rs.internal.subject.Subject;
import io.github.etases.edublock.rs.internal.subject.SubjectManager;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.common.Validate;
import me.hsgamer.hscore.expression.ezylang.ExpressionUtils;
import org.tinylog.Logger;

import java.math.BigDecimal;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Classification {
    String identifier = "";
    String name = "";
    List<String> otherNames = Collections.emptyList();
    int level = Integer.MAX_VALUE;
    List<String> rules = Collections.emptyList();

    public static Classification fromMap(String identifier, Map<String, Object> map) {
        var classification = new Classification();
        classification.setIdentifier(identifier);
        Optional.ofNullable(map.get("name")).map(Objects::toString).ifPresent(classification::setName);
        Optional.ofNullable(map.get("otherNames")).map(CollectionUtils::createStringListFromObject).ifPresent(classification::setOtherNames);
        Optional.ofNullable(map.get("level")).map(Objects::toString).flatMap(Validate::getNumber).map(BigDecimal::intValueExact).ifPresent(classification::setLevel);
        Optional.ofNullable(map.get("rules")).map(CollectionUtils::createStringListFromObject).ifPresent(classification::setRules);
        return classification;
    }

    public boolean isApplicable(Map<Subject, Float> subjectScoreMap) {
        var subjects = SubjectManager.getSubjects();
        Map<String, Object> variableMap = new HashMap<>();
        for (var subject : subjects) {
            variableMap.put("subject" + subject.getId(), subjectScoreMap.getOrDefault(subject, 0.0F));
            variableMap.put(subject.getIdentifier().toLowerCase(Locale.ROOT), subjectScoreMap.getOrDefault(subject, 0.0F));
        }
        variableMap.put("subject_min", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).min().orElse(0));
        variableMap.put("subject_max", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).max().orElse(0));
        variableMap.put("subject_avg", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).average().orElse(0));
        variableMap.put("subject_sum", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).sum());
        variableMap.put("subject_min_not_zero", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).filter(value -> value != 0).min().orElse(0));
        variableMap.put("subject_max_not_zero", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).filter(value -> value != 0).max().orElse(0));
        variableMap.put("subject_avg_not_zero", subjectScoreMap.values().stream().mapToDouble(Float::floatValue).filter(value -> value != 0).average().orElse(0));

        for (String rule : rules) {
            Expression expression = ExpressionUtils.createExpression(rule).withValues(variableMap);
            try {
                if (Boolean.FALSE.equals(expression.evaluate().getBooleanValue())) {
                    return false;
                }
            } catch (EvaluationException | ParseException e) {
                Logger.error(e);
                return false;
            }
        }
        return true;
    }
}
