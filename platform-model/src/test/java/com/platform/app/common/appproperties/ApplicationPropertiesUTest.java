package com.platform.app.common.appproperties;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ApplicationPropertiesUTest {
	private ApplicationProperties applicationProperties;

	@Before
	public void setUpTest() {
		applicationProperties = new ApplicationProperties();
		applicationProperties.init();
	}

	@Test
	public void getDaysBeforeOrderExpiration() {
		assertThat(applicationProperties.getDaysBeforeOrderExpiration(), is(equalTo(7)));
	}

}