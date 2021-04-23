package com.capsilon.incomeanalyzer.automation.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.testcontainers.shaded.org.apache.commons.lang.ObjectUtils.compare;

@SuppressWarnings("rawtypes")
@NoArgsConstructor
@Getter
public class FailureComparison {

    private String fieldName;
    private String expected;
    private String actual;

    public FailureComparison(String fieldName, Comparable expected, Comparable actual) {
        if (compare(expected, actual) != 0) {
            this.fieldName = fieldName;
            if (expected != null)
                this.expected = expected.toString();
            else
                this.expected = "null";
            if (actual != null)
                this.actual = actual.toString();
            else
                this.actual = "null";
        }
    }

    @Override
    public String toString() {
        return String.format("fieldName: %s \texpected: %s \tactual: %s %n", fieldName, expected, actual);
    }
}
