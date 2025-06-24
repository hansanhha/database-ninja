package db.ninja;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class MiniCarrotMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniCarrotMarketApplication.class, args);
    }

}
