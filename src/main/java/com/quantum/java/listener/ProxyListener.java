package com.quantum.java.listener;

import org.openqa.selenium.Capabilities;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebDriverCommandAdapter;

public class ProxyListener extends QAFWebDriverCommandAdapter {

	@Override
	public void beforeInitialize(Capabilities desiredCapabilities) {
		super.beforeInitialize(desiredCapabilities);
		//        System.getProperties().put("http.proxyHost","bcproxy.sgp.com");
		//        System.getProperties().put("http.proxyPort","8080");
		//        System.getProperties().put("https.proxyHost","bcproxy.sgp.com"); 
		//        System.getProperties().put("https.proxyPort","8080");
		 System.setProperty("https.proxyHost", "binnacle.nfcu.net");
	        System.setProperty("https.proxyPort", "8080");    
	}
}
