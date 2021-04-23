package com.capsilon.incomeanalyzer.automation.rest.data.loan.refid;

import com.capsilon.incomeanalyzer.automation.utilities.enums.IncomeFrequency;
import com.capsilon.incomeanalyzer.automation.utilities.enums.ValueType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RestDocumentFrequency {

    private List<RestSnippet> snippets;
    private IncomeFrequency value;
    private ValueType valueType;

    public List<RestSnippet> getSnippets() {
        return new ArrayList<>(snippets);
    }

    public void setSnippets(List<RestSnippet> snippets) {
        this.snippets = snippets; //NOSONAR
    }

    public IncomeFrequency getValue() {
        return value;
    }

    public void setValue(IncomeFrequency value) {
        this.value = value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }
}
