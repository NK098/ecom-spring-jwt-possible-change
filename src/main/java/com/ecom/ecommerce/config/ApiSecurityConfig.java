package com.ecom.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecom.ecommerce.service.UserAuthService;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserAuthService userAuthService;

	@Autowired
	private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	// Configure web security to ignore certain paths	
//	@Override
//	public void configure(WebSecurity web) throws Exception {
//		web.ignoring().antMatchers("/api/public/**", "/h2-console/**");
//	}

	@Autowired
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// Configure authentication manager to use UserAuthService
		auth.userDetailsService(userAuthService).passwordEncoder(getPasswordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			// dont authenticate this particular request
			.authorizeRequests()
				.antMatchers("/api/public/**").permitAll()
				.antMatchers("/h2-console/**").permitAll()
				// all other requests need to be authenticated
				.anyRequest().authenticated()
			.and()
			.exceptionHandling()
				.authenticationEntryPoint(apiAuthenticationEntryPoint)
			.and()
			// make sure we use stateless session; session won't be used to
			// store user's state.
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		//To solve the /h2-console blank page issue
		http.headers().frameOptions().disable();
		
		// Add a filter to validate the tokens with every request
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	//This method is optional and can be used to register custom filters.
	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthFilterRegister(JwtAuthenticationFilter filter) {
	    // Register JwtAuthenticationFilter as a filter
	    FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(filter);
	    registrationBean.setOrder(1); // Set the order of the filter
	    registrationBean.addUrlPatterns("/api/auth/**"); // Set the URL patterns to which the filter should be applied
	    return registrationBean;
	}


	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		// Override authenticationManagerBean to expose the authentication manager bean
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

}
