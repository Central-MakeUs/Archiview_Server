package zero.conflict.archiview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ArchiviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArchiviewApplication.class, args);
	}

}
