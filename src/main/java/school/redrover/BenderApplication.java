package school.redrover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:local.properties")

public class BenderApplication {
    private static final Logger logger = LoggerFactory.getLogger(BenderApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BenderApplication.class, args);
        logger.info("BenderApplication has started successfully!");
    }
}
