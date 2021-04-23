package com.capsilon.incomeanalyzer.automation.utilities.conditions;

import com.capsilon.incomeanalyzer.automation.rest.RestCommons;
import com.capsilon.incomeanalyzer.automation.rest.methods.RestGetLoanData;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

class DisabledIfToggledCondition implements ExecutionCondition {
    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@EnabledIfReachable is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        AnnotatedElement element = context
                .getElement()
                .orElseThrow(IllegalStateException::new);
        return findRepeatableAnnotations(element, EnableIfToggled.class)
                .stream()
                .map(this::disableIfToggled)
                .filter(ConditionEvaluationResult::isDisabled)
                .findAny()
                .orElse(ENABLED_BY_DEFAULT);
    }

    @SuppressWarnings("squid:S3655")
    private ConditionEvaluationResult disableIfToggled(EnableIfToggled annotation) {
        String propertyName = annotation.propertyName().value;
        if (annotation.isSiteGuidNeeded()) {
            propertyName = String.format(propertyName, RestCommons.getSiteGuid());
        }
        boolean negateState = annotation.negateState();
        boolean toggled = RestGetLoanData.getActuatorFeatureToggleValue(propertyName);
        if (negateState) {
            toggled = !toggled;
        }
        if (toggled)
            return enabled(String.format(negateState ? "%s was set to false but state was negated" : "%s is set to true", propertyName));
        else
            return disabled(String.format(negateState ? "%s was set to true but state was negated" : "%s is set to false", propertyName));
    }
}
