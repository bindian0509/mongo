package io.bharat.mongo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
	private static final String TOKEN_TYPE_CLAIM = "token_type";
	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";

	private final SecretKey secretKey;
	private final long accessExpirationMillis;
	private final long refreshExpirationMillis;

	public JwtTokenProvider(
			@Value("${security.jwt.secret:change-me-change-me-change-me-change-me}") String secret,
			@Value("${security.jwt.expiration-ms:3600000}") long accessExpirationMillis,
			@Value("${security.jwt.refresh-expiration-ms:2592000000}") long refreshExpirationMillis) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessExpirationMillis = accessExpirationMillis;
		this.refreshExpirationMillis = refreshExpirationMillis;
	}

	public String generateAccessToken(Authentication authentication) {
		return generateToken(authentication.getName(), accessExpirationMillis, TYPE_ACCESS);
	}

	public String generateRefreshToken(Authentication authentication) {
		return generateToken(authentication.getName(), refreshExpirationMillis, TYPE_REFRESH);
	}

	public Authentication getAuthentication(String token, UserDetailsService userDetailsService) {
		Claims claims = parseClaims(token);
		validateTokenType(claims, TYPE_ACCESS);

		String username = claims.getSubject();

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
	}

	public boolean validateAccessToken(String token) {
		return validateTokenWithType(token, TYPE_ACCESS);
	}

	public boolean validateRefreshToken(String token) {
		return validateTokenWithType(token, TYPE_REFRESH);
	}

	public String extractUsernameFromRefreshToken(String token) {
		Claims claims = parseClaims(token);
		validateTokenType(claims, TYPE_REFRESH);
		return claims.getSubject();
	}

	private String generateToken(String subject, long expiryMillis, String type) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expiryMillis);

		return Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(now)
				.setExpiration(expiry)
				.claim(TOKEN_TYPE_CLAIM, type)
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
	}

	private boolean validateTokenWithType(String token, String expectedType) {
		try {
			Claims claims = parseClaims(token);
			validateTokenType(claims, expectedType);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			log.warn("Invalid JWT token: {}", ex.getMessage());
			return false;
		}
	}

	private Claims parseClaims(String token) {
		Jws<Claims> jws = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
		return jws.getBody();
	}

	private void validateTokenType(Claims claims, String expectedType) {
		String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
		if (!expectedType.equals(tokenType)) {
			throw new JwtException("Unexpected token type: " + tokenType);
		}
	}
}

