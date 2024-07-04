package com.innobridge.ethpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
  scanBasePackageClasses = {
    com.innobridge.ethpay.configuration.ApplicationSpecificSpringComponentScanMarker.class,
    com.innobridge.ethpay.controller.ApplicationSpecificSpringComponentScanMarker.class,
    com.innobridge.ethpay.service.ApplicationSpecificSpringComponentScanMarker.class
  }
)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
