package com.capsilon.incomeanalyzer.automation.utilities;

import com.capsilon.test.commons.resources.TestResources;

import java.io.File;

/**
 * @author Jedrzej "tepa strzala" Gdula
 */
public final class IATestResources {

    private static final TestResources resources = TestResources.getInstance()
            .withClassPathResources(IATestResources.class, "incomeAnalyzer.sampleData");

    private IATestResources() {
    }

    public static String toAbsolutePath(String relativeResourcePath) {
        return resources.toAbsolutePath(relativeResourcePath);
    }

    public static File toAbsoluteFile(String relativeResourcePath) {
        return resources.toAbsoluteFile(relativeResourcePath);
    }
}

