package com.ecom.ecommerce.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecom.ecommerce.models.Product;
import com.ecom.ecommerce.repo.ProductRepo;

@RestController
@RequestMapping("/api/auth/seller")
public class SellerController {

	@Autowired
	ProductRepo productRepo;

	@PostMapping("/product")
	@PreAuthorize("hasRole('ROLE_SELLER')")
	public ResponseEntity<Object> postProduct(@RequestBody Product product) {
//		productRepo.save(product);
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(product.getProductId()).toUri()).build();
	}

	@GetMapping("/product")
	@PreAuthorize("hasRole('ROLE_SELLER')")
	public ResponseEntity<Object> getAllProducts() {
		return ResponseEntity.ok(productRepo.findAll());
	}

	@GetMapping("/product/{productId}")
	@PreAuthorize("hasRole('ROLE_SELLER')")
	public ResponseEntity<Object> getProduct(@PathVariable Integer productId) {
		return ResponseEntity.ok(productRepo.findById(productId));
	}

	@PutMapping("/product")
	@PreAuthorize("hasRole('ROLE_SELLER')")
	public ResponseEntity<Object> putProduct() {
		return null;
	}

	@DeleteMapping("/product/{productId}")
	@PreAuthorize("hasRole('ROLE_SELLER')")
	public void deleteProduct(@PathVariable Integer productId) {
		productRepo.deleteById(productId);
	}
}
