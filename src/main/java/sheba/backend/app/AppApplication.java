package sheba.backend.app;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;


@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(DataSource dataSource) {
        return args -> {
            System.out.println("Datasource properties:");
            System.out.println("URL: " + dataSource.getConnection().getMetaData().getURL());
            System.out.println("Username: " + dataSource.getConnection().getMetaData().getUserName());
            System.out.println("Driver: " + dataSource.getConnection().getMetaData().getDriverName());
        };
    }
}



