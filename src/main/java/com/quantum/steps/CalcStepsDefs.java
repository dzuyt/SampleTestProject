/**
 * 
 */
package com.quantum.steps;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.util.StringUtil;
import com.quantum.listerners.QuantumReportiumListener;
import com.quantum.utils.AppiumUtils;
import com.quantum.utils.ConfigurationUtils;
import com.quantum.utils.ConsoleUtils;
import com.quantum.utils.DeviceUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;


@QAFTestStepProvider
public class CalcStepsDefs {

	@When("add \"(.+)\" to \"(.+)\"")
	public void add_x_to_y(long l1, long l2) {
		//DeviceUtils.getQAFDriver().
		//AppiumUtils.getAndroidDriver();
	//	AppiumUtils.getIOSDriver();	
		QuantumReportiumListener.getReportiumClient().stepStart("Adding "+ l1 + " plus " + l2);        
		click("name="+String.valueOf(l1));
		click("btn.plus");
		click("name="+String.valueOf(l2));
		click("btn.equal");
		
	}
	@When("multiply \"(.+)\" to \"(.+)\"")
	public void multiply_x_to_y(long l1, long l2) {
		//DeviceUtils.getQAFDriver().
		//AppiumUtils.getAndroidDriver();
	//	AppiumUtils.getIOSDriver();	
		QuantumReportiumListener.getReportiumClient().stepStart("Multiply "+ l1 + " X " + l2);
		click("name="+String.valueOf(l1));
		click("btn.multiplication");
		click("name="+String.valueOf(l2));
		click("btn.equal");
		
	}
	@Then("result should be \"(.+)\"")
	public void resultShouldBe(long l1) {
		QuantumReportiumListener.getReportiumClient().stepStart("Verifying result");
		if(verifyText("input.box", "in:" + String.valueOf(l1))){
			QuantumReportiumListener.getReportiumClient().reportiumAssert("Expected results is displayed", true);
		}else{
			QuantumReportiumListener.getReportiumClient().reportiumAssert("Expected results is not displayed", false);
		}
	}
	
	@QAFTestStep(description = "verify {loc} text is {text}")
	public static boolean verifyText(String loc, String text) {
		return getElement(loc).verifyText(text);
	}
	
	@QAFTestStep(description = "click on {loc}")
	public static void click(String loc) {
		getElement(loc).click();
	}
	
	private static QAFExtendedWebElement getElement(String loc) {
		return new QAFExtendedWebElement(loc);
	}
	
	/*@Then("I switch to frame \"(.*?)\"")
	public static void switchToFrame(String nameOrIndex) {
		if (StringUtil.isNumeric(nameOrIndex)) {
			int index = Integer.parseInt(nameOrIndex);
			new WebDriverTestBase().getDriver().switchTo().frame(index);
		} else {
			new WebDriverTestBase().getDriver().switchTo().frame(nameOrIndex);
		}
	}*/

	@Then("I switch to \"(.*?)\" frame by element")
	public static void switchToFrameByElement(String loc) {
		new WebDriverTestBase().getDriver().switchTo().frame(new QAFExtendedWebElement(loc));
	}

	@When("I am using an AppiumDriver")
	public void testForAppiumDriver() {
		QuantumReportiumListener.getReportiumClient().stepStart("I am using an AppiumDriver");
		if (ConfigurationUtils.getBaseBundle().getPropertyValue("driver.name").contains("Remote"))
			ConsoleUtils.logWarningBlocks("Driver is an instance of QAFExtendedWebDriver");
		else if (AppiumUtils.getAppiumDriver() instanceof IOSDriver)
			ConsoleUtils.logWarningBlocks("Driver is an instance of IOSDriver");
		else if (AppiumUtils.getAppiumDriver() instanceof AndroidDriver)
			ConsoleUtils.logWarningBlocks("Driver is an instance of AndroidDriver");
	}

}
