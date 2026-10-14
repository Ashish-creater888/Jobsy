package Job_Portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jobsy.email-worker.enabled=false")
class JobsyApplicationTests {

	@Test
	void contextLoads() {
	}

}
