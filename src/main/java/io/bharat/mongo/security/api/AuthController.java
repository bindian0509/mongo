package io.bharat.mongo.security.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bharat.mongo.security.JwtTokenProvider;
import io.bharat.mongo.security.dto.LoginRequest;
import io.bharat.mongo.security.dto.RefreshRequest;
import io.bharat.mongo.security.dto.TokenPairResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;

	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
			UserDetailsService userDetailsService) {
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
	}

	@PostMapping("/login")
	public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		String accessToken = tokenProvider.generateAccessToken(authentication);
		String refreshToken = tokenProvider.generateRefreshToken(authentication);
		log.info("User authenticated username={}", request.username());

		return ResponseEntity.ok(new TokenPairResponse(accessToken, refreshToken, "Bearer"));
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		if (!tokenProvider.validateRefreshToken(request.refreshToken())) {
			throw new BadCredentialsException("Invalid refresh token");
		}

		String username = tokenProvider.extractUsernameFromRefreshToken(request.refreshToken());
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		String newAccessToken = tokenProvider.generateAccessToken(authentication);
		String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

		log.info("Issued new tokens via refresh for username={}", username);
		return ResponseEntity.ok(new TokenPairResponse(newAccessToken, newRefreshToken, "Bearer"));
	}
}

