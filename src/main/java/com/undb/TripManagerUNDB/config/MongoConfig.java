package com.undb.TripManagerUNDB.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    private static final String FALLBACK_URI =
        "mongodb+srv://admin:admin@cluster0.x5bmhxf.mongodb.net/tripmanager?appName=Cluster0";

    @Bean
    public MongoClient mongoClient() {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            uri = FALLBACK_URI;
        }
        return MongoClients.create(uri);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}