package com.example.airportInfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@EnableCaching
@EnableAsync
public class AirportInfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirportInfoApplication.class, args);
	}
}
