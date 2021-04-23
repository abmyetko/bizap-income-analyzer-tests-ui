package com.capsilon.incomeanalyzer.automation.performance.base;

import com.capsilon.incomeanalyzer.automation.performance.tests.PerformanceCanonicalTest;
import com.capsilon.test.commons.BaseJUnit5PerformanceTest;
import org.jsmart.zerocode.core.domain.LoadWith;
import org.jsmart.zerocode.core.domain.TestMapping;
import org.jsmart.zerocode.core.domain.TestMappings;
import org.jsmart.zerocode.jupiter.extension.ParallelLoadExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.platform.suite.api.IncludeEngines;

import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@ExtendWith({ParallelLoadExtension.class})
@IncludeEngines("junit-jupiter")
@Execution(CONCURRENT)
@ResourceLock(value = "PerformanceTest")
public class PerformanceTest extends BaseJUnit5PerformanceTest {

    @Test
    @LoadWith("test_testing.properties")
    @TestMappings({
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "incomeCategoryShouldBePossibleToSelectDeselect"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "incomeTypeShouldBePossibleToSelectDeselect"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "deselectingPreviousYearShouldDeselectTwoYearPrior"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "deselectingPreviousYearShouldNotDefaultHistoricalAverageToYtdForHourlyIncome"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "selectingYearBeforePriorShouldShowValuesInHaAndDefaultToLowestIncome"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "incomeHistoricalAveragesShouldBePossibleToSelect"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "onlyOneMonthlyIncomeShouldBeSelectedForOneIncomePart"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "applicantCheckShouldSelectOrDeselectHim"),
            @TestMapping(testClass = PerformanceCanonicalTest.class, testMethod = "checkIfAllCanonicalDocumentsHaveBeenImportedCorrectlyToAnnualSummaries")
    })
    public void allTests() {
    }
}
