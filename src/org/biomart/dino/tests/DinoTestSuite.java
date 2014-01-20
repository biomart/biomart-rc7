package org.biomart.dino.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	EnrichmentDinoTest.class, 
	DinoHandlerTest.class })
public class DinoTestSuite {

}
