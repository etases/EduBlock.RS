package io.github.etases.edublock.rs.internal.classification.function;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@FunctionParameter(name = "value", isVarArg = true)
public class AverageFunction extends AbstractFunction {
    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... evaluationValues) {
        BigDecimal averageNumber = Arrays.stream(evaluationValues)
                .map(EvaluationValue::getNumberValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(evaluationValues.length), RoundingMode.HALF_EVEN);
        return new EvaluationValue(averageNumber);
    }
}
