package dev.practice.booksocialnetwork.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("mongo")
public class MongoSchedulingConfig {
}
