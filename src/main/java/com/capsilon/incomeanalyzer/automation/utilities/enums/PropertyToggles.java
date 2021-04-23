package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum PropertyToggles {

    OVERTIME_TOGGLE("overtimeInclusionEnabled"),
    COMMISSIONS_TOGGLE("commissionInclusionEnabled"),
    PDF_GENERATION_TOGGLE("pdfAutoGenerationFlag"),
    SECONDARY_JOBS_TOGGLE("secondaryJobsEnabled"),
    TEACHERS_TOGGLE("teachersIncomeEnabled"),
    BONUS_TOGGLE("bonusInclusionEnabled"),
    UPDATE_EVENT_TOGGLE("dv.site-properties-by-site-guid.%s.feature-toggles.dataSourceUpdateEvent"),
    ELLIE_FILTER_TOGGLE("dv.site-properties-by-site-guid.%s.feature-toggles.eligibilityFilter"),
    AUTOMATIC_LOCK("dv.site-properties-by-site-guid.%s.feature-toggles.automaticLock")
    ;

    public final String value;

    PropertyToggles(String value) {
        this.value = value;
    }
}
