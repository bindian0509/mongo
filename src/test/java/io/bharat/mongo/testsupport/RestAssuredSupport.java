package io.bharat.mongo.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;

public final class RestAssuredSupport {

	private RestAssuredSupport() {
	}

	public static void configureObjectMapper() {
		ObjectMapper mapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		RestAssured.config = RestAssuredConfig.config()
				.objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_2)
						.jackson2ObjectMapperFactory((cls, charset) -> mapper));
	}

	public static RequestSpecification baseSpec(String baseUrl) {
		return new RequestSpecBuilder()
				.setBaseUri(baseUrl)
				.setContentType(ContentType.JSON)
				.build();
	}
}

