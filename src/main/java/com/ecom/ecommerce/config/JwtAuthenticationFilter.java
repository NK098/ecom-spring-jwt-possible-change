package com.ecom.ecommerce.config;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecom.ecommerce.service.UserAuthService;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserAuthService userAuthService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Retrieving the JWT token from the request header
		String tokenHeader = request.getHeader("JWT");

		String username = null;
		String token = null;

		// Checking if the token is present and has the correct format
		if (tokenHeader != null && tokenHeader.startsWith("e")) {
			token = tokenHeader;
			try {
				// Extracting the username from the token
				username = jwtUtil.loadUserNameFromToken(token);
			} catch (IllegalStateException e) {
				System.out.println("Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				System.out.println("JWT Token has expired");
			}
		} else {
			System.out.println("Bearer String not found in token");
		}

		// Checking if the username is not null and the user is not already
		// authenticated
		if (null != username && SecurityContextHolder.getContext().getAuthentication() == null) {
			// Loading the UserDetails for the username
			UserDetails userDetails = userAuthService.loadUserByUsername(username);

			// Validating the token against the UserDetails
			if (jwtUtil.validateToken(token, userDetails)) {
				// Creating an authentication token for the user
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				// Printing the granted authorities for debugging purposes
				String authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.collect(Collectors.joining());
				System.out.println("Authorities granted : " + authorities);
				////

				// Setting the authentication token and details in the SecurityContextHolder
				WebAuthenticationDetails buildDetails = new WebAuthenticationDetailsSource().buildDetails(request);
				authenticationToken.setDetails(buildDetails);
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}

		// Proceeding with the filter chain
		filterChain.doFilter(request, response);
	}
}
