package com.ecom.ecommerce.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecommerce.models.Cart;
import com.ecom.ecommerce.models.Product;
import com.ecom.ecommerce.repo.CartProductRepo;
import com.ecom.ecommerce.repo.CartRepo;
import com.ecom.ecommerce.repo.ProductRepo;

@RestController
@PreAuthorize("hasRole('ROLE_CONSUMER')")
@RequestMapping("/api/auth/consumer")
public class ConsumerController {
	
	@Autowired
	CartRepo cartRepo;
	
	@Autowired
	ProductRepo productRepo;
	
	@Autowired
	CartProductRepo cartProductrepo;
	
	@GetMapping("/cart")
	public Cart getCart() {
		return cartRepo.findAll().get(0);
	}
	
	@PostMapping("/cart")
	public void postCart(@RequestBody Product product) {
		productRepo.save(product);
	}
	
	@PutMapping("/cart")
	public ResponseEntity<Object> putCart() {
		//cartProductrepo.;
		return null;
	}
	
	@DeleteMapping("/cart")
	public void deleteCart(@RequestBody Product product) {
		productRepo.delete(product);
	}
}
