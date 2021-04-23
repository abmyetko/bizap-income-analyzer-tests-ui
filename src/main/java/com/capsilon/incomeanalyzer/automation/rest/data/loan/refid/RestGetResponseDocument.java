package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.utilities.enums.ErrorStatusType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Accessors(chain = true)
public class RestGetResponseDocument {

    private String conversionDateTime;
    private String dataSourceId;
    private String documentId;
    private Map<ErrorStatusType, Set<String>> errors;
    private String employerName;
    private RestDocumentFrequency frequency;
    private Long id;
    private String status;
    private List<String> tags = new ArrayList<>();
    private String type;
    private String payPeriodStartDate;
    private String payPeriodEndDate;

    public Set<String> getOptionalErrors() {
        return errors.get(ErrorStatusType.optional);
    }

    public Set<String> getRequiredErrors() {
        return errors.get(ErrorStatusType.required);
    }
}
