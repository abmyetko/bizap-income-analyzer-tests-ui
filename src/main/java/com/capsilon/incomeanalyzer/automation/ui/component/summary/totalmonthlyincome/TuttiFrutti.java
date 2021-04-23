package com.capsilon.incomeanalyzer.automation.ui.component.summary.totalmonthlyincome;

import com.capsilon.test.run.confiuration.BizappsConfig;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;

import static com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage.firstSubstringSeparatedByNewLineOrSubstringText;
import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.filterOutNonDigits;
import static com.capsilon.incomeanalyzer.automation.utilities.StringUtilities.getDoubleValueOfIncome;

public class TuttiFrutti {

    public final SelenideElement EMPTY_BAR;
    public final SelenideElement BAR;
    public final ElementsCollection BARS_WRAPPER;
    private final int tooltipCollectionSize; //NOSONAR
    private final SelenideElement COMPONENT_CONTAINER; //NOSONAR
    private final ElementsCollection BAR_PARTS;
    private final SelenideElement TOOLTIP; //NOSONAR


    public TuttiFrutti(SelenideElement componentContainer) {
        COMPONENT_CONTAINER = componentContainer; //NOSONAR
        BAR = componentContainer.$x(".//div[contains(@class,'bars') and contains(@style,'width:')]");
        BARS_WRAPPER = componentContainer.$$x(".//div[@class='bars-wrapper']");
        BAR_PARTS = componentContainer.$$x(".//div[contains(@class,'bar-item') and contains(@style,'width:')]");
        TOOLTIP = componentContainer.$x(".//div[@class='tooltip']"); //NOSONAR
        tooltipCollectionSize = componentContainer.$$x(".//div[@class='tooltip']//div[contains(@class,'legend')]").size(); //NOSONAR
        EMPTY_BAR = componentContainer.$(".empty");
    }

    public void hoverOnBar() {
        BAR.hover();
    }

    public double barPartsWidthSum() {
        double sum = 0;
        for (SelenideElement barPart : BAR_PARTS) {
            sum += getDoubleValueOfIncome(filterOutNonDigits(barPart.getAttribute("style"))); //NOSONAR
        }
        return sum;
    }

    public HashMap<String, String> getTooltipList() { //NOSONAR

        if ("ie".equals(BizappsConfig.getString("bizapps.selenium.browser", "chrome")) ||
                "edge".equals(BizappsConfig.getString("bizapps.selenium.browser", "chrome"))) {
            BAR_PARTS.get(0).scrollIntoView(false);
            BAR_PARTS.get(0).toWebElement().click();
        } else {
            BAR_PARTS.get(0).hover();
        }

        HashMap<String, String> hashMap = new HashMap<>();
        for (SelenideElement tooltip : COMPONENT_CONTAINER.$$x(".//div[@class='tooltip']//div[contains(@class,'legend')]")) {
            hashMap.put(firstSubstringSeparatedByNewLineOrSubstringText(tooltip, tooltip.$x(".//span").text()), tooltip.$x(".//span").text().trim());
        }
        return hashMap;
    }

    public double getTooltipIncomeSum() {
        HashMap<String, String> tooltips = getTooltipList();
        double sum = 0;
        for (HashMap.Entry<String, String> key : tooltips.entrySet()) {
            sum += getDoubleValueOfIncome(key.getValue());
        }
        return sum;
    }
}
