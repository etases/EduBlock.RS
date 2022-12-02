package io.github.etases.edublock.rs.internal.subject;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import io.github.etases.edublock.rs.entity.RecordEntry;
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
public class Subject {
    long id;
    String identifier;
    String name;
    List<String> otherNames;
    String firstHalfScoreRule = "0";
    String secondHalfScoreRule = "0";
    String finalScoreRule = "0";

    public static Subject fromMap(Map<String, Object> map) {
        Subject subject = new Subject();
        Optional.ofNullable(map.get("id")).map(Objects::toString).flatMap(Validate::getNumber).map(BigDecimal::longValueExact).ifPresent(subject::setId);
        Optional.ofNullable(map.get("identifier")).map(Objects::toString).ifPresent(subject::setIdentifier);
        Optional.ofNullable(map.get("name")).map(Objects::toString).ifPresent(subject::setName);
        Optional.ofNullable(map.get("otherNames")).map(CollectionUtils::createStringListFromObject).ifPresent(subject::setOtherNames);
        Optional.ofNullable(map.get("firstHalfScoreRule")).map(Objects::toString).ifPresent(subject::setFirstHalfScoreRule);
        Optional.ofNullable(map.get("secondHalfScoreRule")).map(Objects::toString).ifPresent(subject::setSecondHalfScoreRule);
        Optional.ofNullable(map.get("finalScoreRule")).map(Objects::toString).ifPresent(subject::setFinalScoreRule);
        return subject;
    }

    public void updateScore(RecordEntry entry) {
        if (entry.getFirstHalfScore() <= 0) {
            updateHalfScore(entry, true);
        }
        if (entry.getSecondHalfScore() <= 0) {
            updateHalfScore(entry, false);
        }
        if (entry.getFinalScore() <= 0) {
            updateFinalScore(entry);
        }
    }

    private void updateHalfScore(RecordEntry entry, boolean firstHalf) {
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("oral1", firstHalf ? entry.getFirstHalfOral1() : entry.getSecondHalfOral1());
        variableMap.put("oral2", firstHalf ? entry.getFirstHalfOral2() : entry.getSecondHalfOral2());
        variableMap.put("oral3", firstHalf ? entry.getFirstHalfOral3() : entry.getSecondHalfOral3());
        variableMap.put("minute1", firstHalf ? entry.getFirstHalfMinute1() : entry.getSecondHalfMinute1());
        variableMap.put("minute2", firstHalf ? entry.getFirstHalfMinute2() : entry.getSecondHalfMinute2());
        variableMap.put("minute3", firstHalf ? entry.getFirstHalfMinute3() : entry.getSecondHalfMinute3());
        variableMap.put("session1", firstHalf ? entry.getFirstHalfSession1() : entry.getSecondHalfSession1());
        variableMap.put("session2", firstHalf ? entry.getFirstHalfSession2() : entry.getSecondHalfSession2());
        variableMap.put("session3", firstHalf ? entry.getFirstHalfSession3() : entry.getSecondHalfSession3());
        variableMap.put("session4", firstHalf ? entry.getFirstHalfSession4() : entry.getSecondHalfSession4());
        variableMap.put("session5", firstHalf ? entry.getFirstHalfSession5() : entry.getSecondHalfSession5());
        variableMap.put("session6", firstHalf ? entry.getFirstHalfSession6() : entry.getSecondHalfSession6());
        variableMap.put("final", firstHalf ? entry.getFirstHalfFinal() : entry.getSecondHalfFinal());

        String rule = firstHalf ? firstHalfScoreRule : secondHalfScoreRule;
        Expression expression = ExpressionUtils.createExpression(rule).withValues(variableMap);
        float score;
        try {
            score = expression.evaluate().getNumberValue().floatValue();
        } catch (EvaluationException | ParseException e) {
            Logger.error(e, "Error while evaluating the score");
            score = 0;
        }

        if (firstHalf) {
            entry.setFirstHalfScore(score);
        } else {
            entry.setSecondHalfScore(score);
        }
    }

    private void updateFinalScore(RecordEntry entry) {
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("first", entry.getFirstHalfScore());
        variableMap.put("second", entry.getSecondHalfScore());

        Expression expression = ExpressionUtils.createExpression(finalScoreRule).withValues(variableMap);
        float score;
        try {
            score = expression.evaluate().getNumberValue().floatValue();
        } catch (EvaluationException | ParseException e) {
            Logger.error(e, "Error while evaluating the score");
            score = 0;
        }
        entry.setFinalScore(score);
    }
}
