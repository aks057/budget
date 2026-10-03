package com.budwiser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BudwiserBackendApplicationTests extends AbstractIntegrationTest {

	@Test
	@DisplayName("application context starts and Liquibase migrations apply against Postgres")
	void contextLoads() {
	}

}
