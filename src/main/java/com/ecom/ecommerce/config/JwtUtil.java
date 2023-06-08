package com.ecom.ecommerce.config;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ecom.ecommerce.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7206187061330172174L;

	@Value("${jwt.secret}")
	private String secret;

	private static final long EXPIRATION_TIME = 10 * 60 * 60;

	public User getUser(final String token) {
		return null;
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", userDetails.getAuthorities());
		String generatedJWT = Jwts
					.builder()
					.setClaims(claims)
					.setSubject(userDetails.getUsername())
					.setIssuedAt(new Date(System.currentTimeMillis()))
					.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME*1000))
					.signWith(SignatureAlgorithm.HS512, secret)
					.compact();
		System.out.println("Generated JWT is:\t"+generatedJWT);
		return generatedJWT;
	}

	public Boolean validateToken(final String token, UserDetails userDetails) {
		String username = loadUserNameFromToken(token);
		Claims claims = Jwts
							.parser()
							.setSigningKey(secret)
							.parseClaimsJws(token)
							.getBody();
		Boolean isTokenExpired = claims
									.getExpiration()
									.before(new Date());
		return (username.equals(userDetails.getUsername()) && !isTokenExpired);
	}

	public String loadUserNameFromToken(String token) {
		System.out.println("Token received:\t"+token);
		final Claims claims = Jwts
								.parser()
								.setSigningKey(secret)
								.parseClaimsJws(token)
								.getBody();
		return claims.getSubject();
	}
}
