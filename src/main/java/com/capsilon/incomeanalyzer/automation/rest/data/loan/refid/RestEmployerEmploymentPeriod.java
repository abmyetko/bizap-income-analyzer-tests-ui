package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.rest.FailureComparison;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestEmployerEmploymentPeriod {

    private String dateFrom;
    private String dateTo;
    private List<RestSnippet> snippets = new ArrayList<>();
    private String ytdDate;
    private String documentId;
    private List<Integer> footnotesIdx;

    public List<FailureComparison> compareTo(RestEmployerEmploymentPeriod anotherEmployerEmploymentPeriod) {
        List<FailureComparison> failList = new ArrayList<>();
        failList.add(new FailureComparison("applicants.incomes.employer.employmentPeriod.dateFrom", this.getDateFrom(), anotherEmployerEmploymentPeriod.getDateFrom()));
        failList.add(new FailureComparison("applicants.incomes.employer.employmentPeriod.dateTo", this.getDateTo(), anotherEmployerEmploymentPeriod.getDateTo()));
        failList.add(new FailureComparison("applicants.incomes.employer.employmentPeriod.ytdDate", this.getYtdDate(), anotherEmployerEmploymentPeriod.getYtdDate()));
        return failList;
    }
}
