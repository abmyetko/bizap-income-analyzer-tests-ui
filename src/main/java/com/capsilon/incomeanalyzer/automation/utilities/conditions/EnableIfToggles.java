package com.capsilon.incomeanalyzer.automation.utilities.conditions;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, TYPE})
@Retention(RUNTIME)
@ExtendWith(DisabledIfToggledCondition.class)
public @interface EnableIfToggles {
    EnableIfToggled[] value();
}
