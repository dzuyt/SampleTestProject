package com.quantum.java.listener;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.core.QAFTestBase;
import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.quantum.utils.DeviceUtils;

public class TestNGListener implements ITestListener {

	public static Set<String> executionId;

	@Override
	public void onFinish(ITestContext arg0) {
	}

	@Override
	public void onStart(ITestContext arg0) {		
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
		postExecution();
	}

	@Override
	public void onTestFailure(ITestResult arg0) {
		postExecution();
	}

	@Override
	public void onTestSkipped(ITestResult arg0) {
	}

	@Override
	public void onTestStart(ITestResult arg0) {
		setExecutionIds();
	}

	@Override
	public void onTestSuccess(ITestResult arg0) {
		postExecution();
	}


	protected void postExecution(){		
		checkDriver();
	}

	@SuppressWarnings("unchecked")
	protected void setExecutionIds(){
		if(executionId == null){
			executionId = new HashSet<String>();
		}
		executionId.add((String) DeviceUtils.getQAFDriver().getCapabilities().getCapability("executionId"));
		System.out.println("Device execution ids: " + executionId.toString());
	}



	protected void checkDriver(){
		long end = System.currentTimeMillis(); 
		Date startDate = new Date(ConfigurationManager.getBundle().getLong("Driver.startDate"));
		System.out.println("Driver started at: " + startDate.toString());
		Date endDate = new Date(end);
		System.out.println("Driver end time: " + endDate.toString());
		long diffMs = endDate.getTime() - startDate.getTime();
		long diffSec = diffMs / 1000;
		long min = diffSec / 60;
		long sec = diffSec % 60;
		System.out.println("The driver lifetime is "+min+" minutes and "+sec+" seconds.");
		if(min >= 120){			
			TestBaseProvider.instance().get().tearDown();
			System.out.println("Driver Quit successful");
			QAFTestBase.pause(2000);
			TestBaseProvider.instance().get().getUiDriver();
			DeviceUtils.getQAFDriver().updateSessionId();			
			System.out.println("New driver created at: " + System.currentTimeMillis());
			ConfigurationManager.getBundle().setProperty("Driver.startDate", System.currentTimeMillis());

		}
	}

	protected String generatePDF(){
		StringBuilder reportPDFURL = new StringBuilder(
				"https://"
						+  ConfigurationManager.getBundle().getString("perfecto.email.reporting") 
						+"/export/api/v1/test-executions/pdf?");
		int i = 0;

		for (String exID : executionId) {
			if(i !=0) {
				reportPDFURL.append("&");
			}
			reportPDFURL.append("externalId[" + i + "]=" + exID);
			i++;
		}
		System.out.println(reportPDFURL);
		return reportPDFURL.toString();
	}

}
