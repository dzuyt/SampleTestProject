package com.quantum.steps;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.quantum.listerners.QuantumReportiumListener;
import com.quantum.pages.GoogleSearch;
import com.quantum.utils.DeviceUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.appium.java_client.AppiumDriver;

import java.util.List;

import org.openqa.selenium.Keys;

@QAFTestStepProvider
public class GoogleStepDefs {
	QAFExtendedWebDriver driver =WebDriverTestBase().getDriver();
	
	@QAFTestStep(description="I am on Google Search Page")
	public void iAmOnGoogleSearchPage() throws Throwable {
		new WebDriverTestBase().getDriver().get("http://www.google.com/");
		
	}

	private WebDriverTestBase WebDriverTestBase() {
		// TODO Auto-generated method stub
		return null;
	}


	@QAFTestStep(description="I search for {0}")
	public void I_search_for(String searchKey) throws Throwable {
		GoogleSearch google = new GoogleSearch();
		google.getTxtSearch().waitForVisible();
		google.getTxtSearch().clear();
		google.getTxtSearch().sendKeys(searchKey);
		if(ConfigurationManager.getBundle().getString("perfecto.capabilities.platformName").equalsIgnoreCase("mac") || ConfigurationManager.getBundle().getString("perfecto.capabilities.platformName").equalsIgnoreCase("windows")) {
			google.getTxtSearch().sendKeys(Keys.ENTER);
		}else{
			google.getBtnSearch().waitForVisible(30000);google.getBtnSearch().click();
		}
		
	}

	@QAFTestStep(description="it should have {0} in search results")
	public void it_should_have_in_search_results(String result) throws Throwable {
		QAFExtendedWebElement searchResultElement = new QAFExtendedWebElement("partialLink=" + result);
		searchResultElement.waitForVisible(60000);
		DeviceUtils.getQAFDriver().takeScreenShot();

		if(searchResultElement.verifyPresent(result)){			
			QuantumReportiumListener.getReportiumClient().reportiumAssert("Expected result: " + result + " is displayed", true);
		}else{
			QuantumReportiumListener.getReportiumClient().reportiumAssert("Expected results: " + result + " is not displayed", false);
		}
	}

	@QAFTestStep(description="it should have following search results:")
	public void it_should_have_all_in_search_results(List<String> results) {
		QAFExtendedWebElement searchResultElement;
		for (String result : results) {
			searchResultElement = new QAFExtendedWebElement("partialLink=" + result);
			searchResultElement.verifyPresent(result);
		}
	}



}
