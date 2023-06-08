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
import com.ecom.ecommerce.models.JwtRequestModel;
import com.ecom.ecommerce.models.Product;
import com.ecom.ecommerce.repo.ProductRepo;
import com.ecom.ecommerce.service.UserAuthService;

@RestController
@RequestMapping("/api/public")
public class PublicController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserAuthService authService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	ProductRepo productRepo;

	@GetMapping("/product/search")
	public List<Product> getProducts(@RequestParam(required = true) String keyword) {
		return productRepo.findByProductNameContainingIgnoreCaseOrCategoryCategoryNameContainingIgnoreCase(keyword,
				keyword);
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody JwtRequestModel jwtRequest) throws Exception {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword()));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}

		final UserDetails userDetails = authService.loadUserByUsername(jwtRequest.getUsername());
		final String token = jwtUtil.generateToken(userDetails);
		return ResponseEntity.ok(token);
	}

}
