package com.youtube.chanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChanalyzerApplication {
	public static final Logger LOG = LoggerFactory.getLogger(ChanalyzerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ChanalyzerApplication.class, args);
	}

}
