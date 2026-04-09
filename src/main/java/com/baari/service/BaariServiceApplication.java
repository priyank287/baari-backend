package com.baari.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.baari")
@EnableJpaRepositories(basePackages = "com.baari.app.repository")
public class BaariServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaariServiceApplication.class, args);
	}

}
