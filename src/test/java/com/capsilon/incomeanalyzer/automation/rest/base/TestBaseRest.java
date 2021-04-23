package com.capsilon.incomeanalyzer.automation.rest.base;

import com.capsilon.automation.dv.helpers.AsyncDVClient;
import com.capsilon.automation.dv.helpers.AsyncDVClientTestConfiguration;
import com.capsilon.automation.dv.helpers.ClientConfigBridge;
import com.capsilon.incomeanalyzer.automation.data.upload.DataUploadObject;
import com.capsilon.incomeanalyzer.automation.rest.RestCommons;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFNMBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAFolderBuilder;
import com.capsilon.incomeanalyzer.automation.utilities.builders.IAMismoBuilder;
import com.capsilon.reportportal.junit5.ReportPortalExtension;
import com.capsilon.test.commons.BaseJUnit5RestTest;
import com.capsilon.test.run.confiuration.BizappsConfig;
import com.capsilon.test.ui.Retry;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zeroturnaround.exec.stream.slf4j.Slf4jInfoOutputStream;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import static com.capsilon.incomeanalyzer.automation.rest.RestCommons.REST_URL;
import static com.capsilon.incomeanalyzer.automation.utilities.CommonMethods.TIMEOUT_FIFTEEN_SECONDS;

@SuppressWarnings("rawtypes")
@ContextConfiguration(classes = AsyncDVClientTestConfiguration.class)
@Extensions({
        @ExtendWith(ReportPortalExtension.class),
        @ExtendWith(SpringExtension.class)
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestBaseRest extends BaseJUnit5RestTest {

    static {
        // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();
        // Load Configuration Of Web Core Components from BizApp Config
        ClientConfigBridge.init();
    }

    protected final Gson gson = RestCommons.getGsonConfig();
    private final Logger logger = LoggerFactory.getLogger(TestBaseRest.class);
    protected DataUploadObject dataUpload;
    private Boolean isAttemptingLogin = false;

    @Autowired
    public AsyncDVClient dvFolderClient;

    @BeforeAll
    public void setDefaults() throws UnsupportedEncodingException {
        if (!isAttemptingLogin) {
            isAttemptingLogin = true;
            setBaseRestConfig(logger);
            Retry.tryRun(TIMEOUT_FIFTEEN_SECONDS, RestCommons::loginToBizapps);
            isAttemptingLogin = false;
        } else {
            Retry.whileTrue(TIMEOUT_FIFTEEN_SECONDS, () -> RestCommons.getRestCookiesWithoutLogin() == null, "First attempt not logged in in time");
        }
    }

    public DataUploadObject createUploadObject(IAFolderBuilder builder) {
        return createUploadObject(builder, dvFolderClient);
    }

    public DataUploadObject createUploadObject(IAFolderBuilder builder, AsyncDVClient dvFolderClient) {
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

    private void setBaseRestConfig(Logger restLogger) throws UnsupportedEncodingException {
        RestAssured.baseURI = REST_URL;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        PrintStream printStream = new PrintStream(new Slf4jInfoOutputStream(restLogger), false, "UTF-8");
    }
}
