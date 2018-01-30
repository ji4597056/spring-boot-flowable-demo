package com.github.ji4597056;

import org.flowable.engine.RepositoryService;
import org.flowable.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * spring boot starter
 *
 * @author Jeffrey
 * @since 2018/1/26 15:32
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class FlowableDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService) {
        return strings -> System.out.println("Number of process definitions : "
            + repositoryService.createProcessDefinitionQuery().count());
    }
}
