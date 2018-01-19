@Appium 
Feature: Appium Example Feature 
#Sample Test Scenario Description

@Appium_Add
Scenario: Appium Example 
	Given I start application by name "Calculator" 
	    And I am using an AppiumDriver
	    When add "3" to "5"
	    Then result should be "8"
	    Then I close application by name "Calculator"

@Appium_Multiply
Scenario: Appium Example 
	Given I start application by name "Calculator" 
	    And I am using an AppiumDriver
	    When multiply "3" to "5"
	    Then result should be "15"
	    Then I close application by name "Calculator"