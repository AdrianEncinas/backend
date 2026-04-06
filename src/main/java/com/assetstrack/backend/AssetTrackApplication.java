package com.assetstrack.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AssetTrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssetTrackApplication.class, args);
	}

}
