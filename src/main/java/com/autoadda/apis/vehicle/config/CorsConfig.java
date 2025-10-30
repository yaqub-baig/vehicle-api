package com.autoadda.apis.vehicle.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// More secure CORS configuration
		config.setAllowedOriginPatterns(Arrays.asList(
				"*"
		));

		config.setAllowedMethods(Arrays.asList(
				"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
		));

		config.setAllowedHeaders(Arrays.asList(
				"Authorization",
				"Content-Type",
				"X-Requested-With",
				"X-Auto-Adda-Id",
				"accept",
				"Origin",
				"Access-Control-Request-Method",
				"Access-Control-Request-Headers",
				"skip_zrok_interstitial"
		));

		config.setExposedHeaders(Arrays.asList(
				"Access-Control-Allow-Origin",
				"Access-Control-Allow-Credentials"
		));

		config.setAllowCredentials(false);
		config.setMaxAge(3600L); // Cache preflight response for 1 hour

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}