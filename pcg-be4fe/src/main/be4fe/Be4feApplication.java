package be4fe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "be4fe",
        "common"
})
@EnableJpaRepositories(basePackages = "common.repository")
@EntityScan(basePackages = "common.entity")
public class Be4feApplication {

    public static void main(String[] args) {
        SpringApplication.run(Be4feApplication.class, args);
    }

}
