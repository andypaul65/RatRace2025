package com.finmodel;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.finmodel.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports/cucumber.html,json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class CucumberTestRunner {
    // This class remains empty as it serves only as a holder for the Cucumber options
}