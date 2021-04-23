package com.capsilon.incomeanalyzer.automation.utilities.enums;

public enum SummaryDocumentType {

    PAYSTUB("Paystub"),
    VOE("Verification Of Employment"),
    W2("IRS W-2"),
    LOAN_APPLICATION_FNM("Loan Application"),
    EVOE("Electronic Verification Of Employment"),
    VOE_PREVIOUS("Verification Of Employment"),
    NO_DOC("-"),
    URLA("Urla"),
    MISMO_3_4("MISMO_3_4"),
    INVALID("Invalid document name");

    public final String value;

    SummaryDocumentType(String value) {
        this.value = value;
    }

    public static SummaryDocumentType valueOfLabel(String value) {
        for (SummaryDocumentType e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return INVALID;
    }
}
