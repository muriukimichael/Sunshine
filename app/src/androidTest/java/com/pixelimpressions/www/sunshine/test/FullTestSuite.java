package com.pixelimpressions.www.sunshine.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;

/**
 * Created by mikie on 11/7/14.
 * This class allows us to write tests for all
 * the classes in the application
 */
public class FullTestSuite {

    public FullTestSuite() {
        super();
    }

    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }

}
