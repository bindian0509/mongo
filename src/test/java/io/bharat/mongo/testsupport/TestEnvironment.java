package io.bharat.mongo.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Loads test configuration from {@code test.properties} with environment variable overrides.
 */
public final class TestEnvironment {

	private final Properties properties;

	private TestEnvironment(Properties properties) {
		this.properties = properties;
	}

	public static TestEnvironment load() {
		try (InputStream in = TestEnvironment.class.getClassLoader().getResourceAsStream("test.properties")) {
			Properties props = new Properties();
			if (in != null) {
				props.load(in);
			}
			return new TestEnvironment(mergeEnvOverrides(props));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load test.properties", e);
		}
	}

	private static Properties mergeEnvOverrides(Properties base) {
		Properties merged = new Properties();
		merged.putAll(base);
		for (String key : base.stringPropertyNames()) {
			String envKey = toEnvKey(key);
			String value = System.getenv(envKey);
			if (value != null && !value.isBlank()) {
				merged.setProperty(key, value.trim());
			}
		}
		return merged;
	}

	private static String toEnvKey(String key) {
		return key.replace('.', '_')
				.replace('-', '_')
				.toUpperCase(Locale.ROOT);
	}

	public String baseUrl() {
		return properties.getProperty("test.api.base-url", "http://localhost:8080").trim();
	}

	public String employeesPath() {
		return properties.getProperty("test.api.employees-path", "/api/employees").trim();
	}

	public String loginPath() {
		return properties.getProperty("test.auth.login-path", "/api/auth/login").trim();
	}

	public String refreshPath() {
		return properties.getProperty("test.auth.refresh-path", "/api/auth/refresh").trim();
	}

	public String username() {
		return properties.getProperty("test.auth.username", "admin").trim();
	}

	public String password() {
		return properties.getProperty("test.auth.password", "password").trim();
	}

	/**
	 * Resolves the base URL. If an explicit URL is configured, it is returned; otherwise a local URL is built with the provided port.
	 */
	public String resolveBaseUrl(int localPort) {
		String override = overrideValue("test.api.base-url");
		if (override != null && !override.isBlank()) {
			return trimTrailingSlash(override);
		}

		String configured = baseUrl();
		// Prefer the test server's random port when pointing at localhost.
		if (configured.contains("localhost")) {
			return "http://localhost:" + localPort;
		}

		return trimTrailingSlash(configured);
	}

	private String trimTrailingSlash(String url) {
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		}
		return url;
	}

	private String overrideValue(String key) {
		String envValue = System.getenv(toEnvKey(key));
		if (envValue != null && !envValue.isBlank()) {
			return envValue.trim();
		}
		String systemValue = System.getProperty(key);
		if (systemValue != null && !systemValue.isBlank()) {
			return systemValue.trim();
		}
		return null;
	}
}

