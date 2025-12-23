package io.bharat.mongo.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Test configuration that provides an authenticated MongoClient,
 * overriding any auto-configured unauthenticated connection.
 */
@TestConfiguration
public class TestMongoConfig {

	private static final String HOST = "localhost";
	private static final int PORT = 27017;
	private static final String DATABASE = "mydatabase";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "changeit";
	private static final String AUTH_DATABASE = "admin";

	@Bean
	@Primary
	public MongoClient mongoClient() {
		MongoCredential credential = MongoCredential.createCredential(
				USERNAME,
				AUTH_DATABASE,
				PASSWORD.toCharArray()
		);

		MongoClientSettings settings = MongoClientSettings.builder()
				.applyToClusterSettings(builder ->
						builder.hosts(java.util.List.of(new ServerAddress(HOST, PORT))))
				.credential(credential)
				.build();

		return MongoClients.create(settings);
	}

	@Bean
	@Primary
	public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
		return new SimpleMongoClientDatabaseFactory(mongoClient, DATABASE);
	}

	@Bean
	@Primary
	public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
		return new MongoTemplate(mongoDatabaseFactory);
	}
}

