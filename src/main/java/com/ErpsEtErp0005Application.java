package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper; // Standard Jackson

@SpringBootApplication
public class ErpsEtErp0005Application {

	public static void main(String[] args) {
		SpringApplication.run(ErpsEtErp0005Application.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
