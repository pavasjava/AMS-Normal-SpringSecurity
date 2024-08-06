package com.ams.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf((csrf) -> csrf.disable())
				.authorizeHttpRequests(
						(auth) -> auth.requestMatchers("/employees/**").authenticated().anyRequest().permitAll())
				.formLogin(formLogin -> formLogin.loginPage("/login").defaultSuccessUrl("/employees/getAllEmployee", true).permitAll());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder encoder) {
		UserDetails admin = User.withUsername("adarsh").password(encoder.encode("1234")).build();
		UserDetails superadmin = User.withUsername("pravas").password(encoder.encode("1234")).build();
		return new InMemoryUserDetailsManager(admin, superadmin);
	}

}
