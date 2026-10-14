package Job_Portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class  JobsyApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobsyApplication.class, args);
	}

}
