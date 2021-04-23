package com.capsilon.incomeanalyzer.automation.data.upload.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@SuppressWarnings("unchecked")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DocumentData<T extends DocumentData<T>> {

    protected String documentName;
    protected String borrowerFullName;
    protected String borrowerSSN;
    protected String collaboratorId;
    protected String employerName;

    public DocumentData(String documentName) {
        this.documentName = documentName;
    }

    public String getBorrowerFullName() {
        return borrowerFullName;
    }

    public T setBorrowerFullName(String borrowerFullName) {
        this.borrowerFullName = borrowerFullName;
        return (T) this;
    }

    public String getBorrowerSSN() {
        return borrowerSSN;
    }

    public T setBorrowerSSN(String borrowerSSN) {
        this.borrowerSSN = borrowerSSN;
        return (T) this;
    }

    public String getCollaboratorId() {
        return collaboratorId;
    }

    public T setCollaboratorId(String collaboratorId) {
        this.collaboratorId = collaboratorId;
        return (T) this;
    }

    public String getEmployerName() {
        return employerName;
    }

    public T setEmployerName(String employerName) {
        this.employerName = employerName;
        return (T) this;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
