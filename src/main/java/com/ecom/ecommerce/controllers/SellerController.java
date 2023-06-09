package com.ecom.ecommerce.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecom.ecommerce.config.JwtUtil;
import com.ecom.ecommerce.models.Product;
import com.ecom.ecommerce.models.User;
import com.ecom.ecommerce.repo.ProductRepo;

@RestController
@PreAuthorize("hasRole('ROLE_SELLER')")
@RequestMapping("/api/auth/seller")
public class SellerController {

	@Autowired
	ProductRepo productRepo;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/product")
	public ResponseEntity<Object> postProduct(@RequestHeader("JWT") String token, @RequestBody Product product) {
		User user = jwtUtil.getUser(token);
		product.setSeller(user);
		Product savedProduct = productRepo.save(product);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(savedProduct.getProductId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@GetMapping("/product")
	public ResponseEntity<Object> getAllProducts(@RequestHeader("JWT") String token) {
		User user = jwtUtil.getUser(token);
		List<Product> findBySellerUserId = productRepo.findBySellerUserId(user.getUserId());
		return ResponseEntity.ok(findBySellerUserId);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<Object> getProduct(@RequestHeader("JWT") String token, @PathVariable Integer productId) {
		User user = jwtUtil.getUser(token);
		Optional<Product> findBySellerUserIdAndProductId = productRepo.findBySellerUserIdAndProductId(user.getUserId(),
				productId);
		if (findBySellerUserIdAndProductId.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(findBySellerUserIdAndProductId);
	}

	@PutMapping("/product")
	public ResponseEntity<Object> putProduct(@RequestHeader("JWT") String token, @RequestBody Product product) {
		Optional<Product> findById = productRepo.findById(product.getProductId());
		if (findById.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		productRepo.save(product);
		return ResponseEntity.ok().build();
	}

	// TODO
	@DeleteMapping("/product/{productId}")
	public ResponseEntity<Object> deleteProduct(@RequestHeader("JWT") String token, @PathVariable Integer productId) {
		User user = jwtUtil.getUser(token);
		// Check if the product exists and is owned by the seller
		Optional<Product> productOptional = productRepo.findBySellerUserIdAndProductId(user.getUserId(), productId);
		if (productOptional.isPresent()) {
			// Delete the product
			productRepo.deleteById(productId);
			return ResponseEntity.status(HttpStatus.OK).build();
		} else {
			// Product not found or not owned by the seller, return 404 status code
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

}
