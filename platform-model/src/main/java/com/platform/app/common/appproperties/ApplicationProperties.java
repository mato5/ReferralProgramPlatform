package com.platform.app.common.appproperties;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ApplicationProperties {

	private Properties properties;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void init() {
		try {
			properties = new Properties();
			properties.load(ApplicationProperties.class.getClassLoader().getResourceAsStream("application.properties"));
		} catch (final IOException e) {
			logger.error("Error while reading appproperties file", e);
			throw new IllegalArgumentException(e);
		}
	}

	public int getDaysBeforeOrderExpiration() {
		return Integer.valueOf(properties.getProperty("days-before-order-expiration"));
	}

}