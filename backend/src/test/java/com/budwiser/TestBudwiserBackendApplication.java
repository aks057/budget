package com.budwiser;

import org.springframework.boot.SpringApplication;

public class TestBudwiserBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(BudwiserBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
