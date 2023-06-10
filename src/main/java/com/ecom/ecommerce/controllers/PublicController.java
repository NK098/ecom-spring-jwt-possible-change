package com.ecom.ecommerce.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecommerce.config.JwtUtil;
import com.ecom.ecommerce.models.Product;
import com.ecom.ecommerce.models.User;
import com.ecom.ecommerce.repo.ProductRepo;
import com.ecom.ecommerce.service.UserAuthService;

@RestController
@RequestMapping("/api/public")
public class PublicController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserAuthService userAuthService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	ProductRepo productRepo;

	// Endpoint to search for products based on a keyword
	@GetMapping("/product/search")
	public List<Product> getProducts(@RequestParam(required = true) String keyword) {
		return productRepo.findByProductNameContainingIgnoreCaseOrCategoryCategoryNameContainingIgnoreCase(keyword,
				keyword);
	}

	// Endpoint for user login
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) throws Exception {
		// Authenticating the user using the AuthenticationManager
		try {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					user.getUsername(), user.getPassword());
			authenticationManager.authenticate(authentication);
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}

		// Loading the UserDetails for the authenticated user
		final UserDetails userDetails = userAuthService.loadUserByUsername(user.getUsername());

		// Generating a JWT token using the JwtUtil
		final String token = jwtUtil.generateToken(userDetails);

		// Returning the generated token in the response
		return ResponseEntity.ok(token);
	}

}
