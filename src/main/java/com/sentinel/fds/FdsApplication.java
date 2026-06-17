package com.sentinel.fds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FdsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdsApplication.class, args);
	}

}
