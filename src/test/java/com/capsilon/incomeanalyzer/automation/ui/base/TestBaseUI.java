package com.capsilon.incomeanalyzer.automation.ui.base;

import com.capsilon.automation.bam.rest.admin.BizAppDataProvider;
import com.capsilon.automation.bam.ui.container.ContainerPage;
import com.capsilon.automation.bam.ui.pipeline.pages.AngularLoginPage;
import com.capsilon.automation.bam.ui.pipeline.pages.PipelinePage;
import com.capsilon.automation.dv.helpers.AsyncDVClient;
import com.capsilon.automation.dv.helpers.AsyncDVClientTestConfiguration;
import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.ui.IncomeAnalyzerPage;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFNMBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAMismoBuilder;
import com.capsilon.reportportal.junit5.ReportPortalExtension;
import com.capsilon.test.commons.selenide.SelenideJUnit5TestBase;
import com.capsilon.test.run.confiuration.BizappsConfig;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;

import static com.capsilon.test.ui.components.Browser.goToParentFrame;
import static com.capsilon.test.ui.components.Browser.waitForBizappTabAndSwitchToIt;
import static com.codeborne.selenide.Selenide.open;


@SuppressWarnings("rawtypes")
@ContextConfiguration(classes = AsyncDVClientTestConfiguration.class)
@Extensions({
        @ExtendWith(ReportPortalExtension.class),
        @ExtendWith(SpringExtension.class)
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //required by BrowserStack while PER_CLASS used
@Tag("BrowserStackTag")
public abstract class TestBaseUI extends SelenideJUnit5TestBase {

    protected DataUploadObject dataUpload;
    private static PipelinePage pipelinePage = new PipelinePage();
    private static BizAppDataProvider bizAppDataProvider = new BizAppDataProvider();

    @Autowired
    public AsyncDVClient dvFolderClient;

    protected static void loginCreateLoanAndGoToIncomeAnalyzerBizapp(String loanDocFilePath) {
        loginCreateLoanAndGoToFiledDocsBizapp(loanDocFilePath);
        goToWorkspaceWithIncomeAnalyzer();
    }

    protected static void goToWorkspaceWithIncomeAnalyzer() {
        waitForBizappTabAndSwitchToIt();
        goToParentFrame();
        pipelinePage.changeWorkspace(bizAppDataProvider.findWorkspaceByAppName("WEB_INCOME_ANALYZER"));
    }

    protected static void loginCreateLoanAndGoToFiledDocsBizapp(String folderId) {
        AngularLoginPage.openAndLogin(BizappsConfig.getUnderwriterUser(), BizappsConfig.getUnderwriterUserPassword());
        String folderUrl = (BizappsConfig.getBaseUrl() + "/bac/?folder=" + folderId).replace("//", "/");
        if ("firefox".equals(BizappsConfig.getString("bizapps.selenium.browser"))) {
            open(folderUrl.replace("https", "http"));
        } else {
            open(folderUrl);
        }
    }

    protected static String getFolderId() {
        String browserId = BizappsConfig.getString("bizapps.selenium.browser", "ie");
        String defaultFolderId = BizappsConfig.getString("ia.folderId", "LN-FX1810168");
        String browserFolderId = BizappsConfig.getString("ia." + browserId + ".folderId", defaultFolderId);
        return BizappsConfig.getString("bizapps.force.folderId", browserFolderId);
    }

    public DataUploadObject createUploadObject(IAFolderBuilder builder) {
        return createUploadObject(builder, null, dvFolderClient);
    }

    public DataUploadObject createUploadObject(IAFolderBuilder builder, AsyncDVClient dvFolderClient) {
        return createUploadObject(builder, null, dvFolderClient);
    }

    public DataUploadObject createUploadObject(IAFolderBuilder builder, String folderId, AsyncDVClient dvFolderClient) {
        if (folderId != null) {
            builder.setFolderId(folderId);
        }
        try {
            return (DataUploadObject) Class.forName("com.capsilon.incomeanalyzer.automation.data.upload." +
                    BizappsConfig.getString("bizapps.ia.dataImportType", "JsonUpload"))
                    .getConstructor(IAFolderBuilder.class, AsyncDVClient.class)
                    .newInstance(builder, dvFolderClient);
        } catch (NoSuchMethodException |
                ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                InvocationTargetException e) {
            e.printStackTrace();
            try {
                return (DataUploadObject) Class.forName("com.capsilon.incomeanalyzer.automation.data.upload.JsonUpload")
                        .getConstructor(IAFolderBuilder.class, AsyncDVClient.class)
                        .newInstance(builder, dvFolderClient);
            } catch (NoSuchMethodException |
                    IllegalAccessException |
                    InstantiationException |
                    InvocationTargetException |
                    ClassNotFoundException ignore) {
            }
        }
        return null;
    }

    public IAFolderBuilder createFolderBuilder() {
        return createFolderBuilder(null);
    }

    public IAFolderBuilder createFolderBuilder(String caseNumber) {
        String documentType = BizappsConfig.getString("bizapps.LoanDocumentType", "mismo");
        if (documentType.equalsIgnoreCase("fnm")) {
            return caseNumber == null ? new IAFNMBuilder() : new IAFNMBuilder(caseNumber);
        } else {
            return caseNumber == null ? new IAMismoBuilder() : new IAMismoBuilder(caseNumber);
        }
    }

    @AfterAll
    void closeBrowser() {
        Selenide.closeWebDriver();
    }

    public void refreshFolder() {
        ContainerPage.leftMenuView.openBizap(ContainerPage.leftMenuView.leftMenu.incomeAnalyzer, IncomeAnalyzerPage.summaryView.summary.COMPONENT_CONTAINER);
    }
}
