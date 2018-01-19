package com.quantum.pages;

import java.util.List;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.api.PageLocator;
import com.qmetry.qaf.automation.ui.api.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;
import com.quantum.utils.DeviceUtils;

public class GoogleSearch extends WebDriverBaseTestPage<WebDriverTestPage>{
	@FindBy(locator = "txt.search")
	private QAFWebElement txtSearch;
	@FindBy(locator = "btn.search")
	private QAFWebElement btnSearch;
	
	@Override
	protected void openPage(PageLocator pageLocator, Object... arg1) {
		}	
	
	public QAFWebElement getTxtSearch(){
		return txtSearch;
	}
	public QAFWebElement getBtnSearch(){
		return btnSearch;
	}
	
	

}
