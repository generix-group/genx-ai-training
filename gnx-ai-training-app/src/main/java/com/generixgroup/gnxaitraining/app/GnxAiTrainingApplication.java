package com.generixgroup.gnxaitraining.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.generixgroup.gnxaitraining")
@EnableJpaRepositories(basePackages = "com.generixgroup.gnxaitraining.infrastructure.persistence")
@EntityScan("com.generixgroup.gnxaitraining.domain")
public class GnxAiTrainingApplication {

  public static void main(String[] args) {
    SpringApplication.run(GnxAiTrainingApplication.class, args);
  }
}
