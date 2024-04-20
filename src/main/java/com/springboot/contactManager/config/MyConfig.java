package com.springboot.contactManager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

	private static Logger logger = LoggerFactory.getLogger(MyConfig.class);

	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailSerivceImpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

		return daoAuthenticationProvider;

	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(authorizeRequests -> authorizeRequests

				.requestMatchers("/user/**").hasAuthority("User_RO") // Requires USER role
				// .requestMatchers("/admin").hasRole("ADMIN") // Requires ADMIN role
				.requestMatchers("/**").permitAll() // Allow public URLs
		// .anyRequest().authenticated() // Requires authentication for other URLs
		)
				// .formLogin(Customizer.withDefaults());
				.formLogin(login -> login
						.loginPage("/signin")
						.loginProcessingUrl("/login.do")
						.defaultSuccessUrl("/user/index"));
		// .failureUrl("/login.do"));
		logger.info("In Security Chain...");

		return http.build();
	}

}
