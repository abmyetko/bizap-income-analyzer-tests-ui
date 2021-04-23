package com.capsilon.incomeanalyzer.automation.utilities.conditions;

import com.capsilon.incomeanalyzer.automation.utilities.enums.PropertyToggles;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, TYPE})
@Retention(RUNTIME)
@ExtendWith(DisabledIfToggledCondition.class)
@Repeatable(value = EnableIfToggles.class)
public @interface EnableIfToggled {

    PropertyToggles propertyName();

    boolean negateState() default false;

    boolean isSiteGuidNeeded() default false;
}
