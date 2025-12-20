package io.bharat.mongo.security.dto;

public record TokenPairResponse(String accessToken, String refreshToken, String tokenType) {
}

