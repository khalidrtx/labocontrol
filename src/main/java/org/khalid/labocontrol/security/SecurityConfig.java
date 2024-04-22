package org.khalid.labocontrol.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.khalid.labocontrol.service.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${jwt.secret}")
	private String secretKey;
	@Autowired
	private CustomUserDetailsService customUserDetailsService;

/*	@Bean
	@Primary
	public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
		PasswordEncoder passwordEncoder=passwordEncoder();
		return new InMemoryUserDetailsManager(
				User.withUsername("khalid").password(passwordEncoder.encode("12345")).authorities("ADMIN").build(),
				User.withUsername("ayoub").password(passwordEncoder.encode("KHALID")).authorities("USER").build()
			);

	}*/
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(a->a.requestMatchers("/auth/login/**","/login","/","/register","/css/**", "/js/**", "/images/**").permitAll())
				.authorizeHttpRequests(ar->ar.anyRequest().authenticated())
				//.httpBasic(Customizer.withDefaults())
				.oauth2ResourceServer(oa->oa.jwt(Customizer.withDefaults()))
				.build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
	    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getBytes()));
	}
	
	@Bean
	JwtDecoder jwtEDecoder() {
	    SecretKeySpec secretKeySpec=new SecretKeySpec(secretKey.getBytes(),"RSA");
		return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
		
	}
	
    @Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
		DaoAuthenticationProvider daoAuthenticationProvider= new DaoAuthenticationProvider();
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		daoAuthenticationProvider.setUserDetailsService(userDetailsService);
		return new ProviderManager(daoAuthenticationProvider);
	}

}
