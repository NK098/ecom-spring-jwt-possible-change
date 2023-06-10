package com.ecom.ecommerce.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecommerce.config.JwtUtil;
import com.ecom.ecommerce.models.Cart;
import com.ecom.ecommerce.models.CartProduct;
import com.ecom.ecommerce.models.Product;
import com.ecom.ecommerce.models.User;
import com.ecom.ecommerce.repo.CartProductRepo;
import com.ecom.ecommerce.repo.CartRepo;
import com.ecom.ecommerce.repo.ProductRepo;

@RestController
@PreAuthorize("hasRole('ROLE_CONSUMER')")
@RequestMapping("/api/auth/consumer")
public class ConsumerController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	CartRepo cartRepo;

	@Autowired
	ProductRepo productRepo;

	@Autowired
	CartProductRepo cartProductRepo;

	@GetMapping("/cart")
	public ResponseEntity<Object> getCart(@RequestHeader("JWT") String token) {
		User user = jwtUtil.getUser(token);
		Optional<Cart> findByUserUsername = cartRepo.findByUserUsername(user.getUsername());
		Cart cart = null;
		if (findByUserUsername.isPresent()) {
			cart = findByUserUsername.get();
		}
		return ResponseEntity.ok(cart);
	}

	@PostMapping("/cart")
	public ResponseEntity<Object> postCart(@RequestHeader("JWT") String token, @RequestBody Product product) {
		User user = jwtUtil.getUser(token);
		product.setSeller(user);
		// Find the user's cart
		Cart cart = null;
		Optional<Cart> findByUserUsername = cartRepo.findByUserUsername(user.getUsername());
		if (findByUserUsername.isPresent()) {
			cart = findByUserUsername.get();
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		// Find the product to be added to the cart
		Product existingProduct = productRepo.findById(product.getProductId()).orElse(null);
		if (existingProduct == null) {
			return ResponseEntity.notFound().build();
		}

		// Check if the product is already in the cart
		CartProduct cartProduct = cartProductRepo
				.findByCartUserUserIdAndProductProductId(cart.getUser().getUserId(), existingProduct.getProductId())
				.orElse(null);
		if (cartProduct != null) {
			// Product is already present in the cart
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} else {
			// Create a new cart product and add it to the cart
			cartProduct = new CartProduct(cart, existingProduct, 1);
			cart.getCartProducts().add(cartProduct);
		}

		// Update the total amount of the cart
		cart.updateTotalAmount(existingProduct.getPrice());

		// Save the cart and the cart product
		cartRepo.save(cart);
		cartProductRepo.save(cartProduct);
		return ResponseEntity.ok(cartRepo.findById(cart.getUser().getUserId()));
	}

	@PutMapping("/cart")
	public ResponseEntity<Object> putCart(@RequestHeader("JWT") String token, @RequestBody CartProduct cartProduct) {
		User user = jwtUtil.getUser(token);
		// Retrieve the user's cart
		Optional<Cart> optionalCart = cartRepo.findByUserUsername(user.getUsername());

		if (optionalCart.isPresent()) {
			Cart cart = optionalCart.get();

			// Retrieve the product from the cart product
			Product product = cartProduct.getProduct();

			// Check if the product is already in the cart
			Optional<CartProduct> optionalExistingCartProduct = cartProductRepo
					.findByCartUserUserIdAndProductProductId(cart.getUser().getUserId(), product.getProductId());

			if (cartProduct.getQuantity() == 0) {
				// If the quantity is zero, delete the product from the cart
				if (optionalExistingCartProduct.isPresent()) {
					CartProduct existingCartProduct = optionalExistingCartProduct.get();
					cart.getCartProducts().remove(existingCartProduct);
					cartProductRepo.delete(existingCartProduct);
					cart.updateTotalAmount(-existingCartProduct.getProduct().getPrice());
				}
			} else {
				if (optionalExistingCartProduct.isPresent()) {
					// If the product is already in the cart, update its quantity
					CartProduct existingCartProduct = optionalExistingCartProduct.get();
					cart.updateTotalAmount(
							(cartProduct.getQuantity() - existingCartProduct.getQuantity()) * product.getPrice());
					existingCartProduct.setQuantity(cartProduct.getQuantity());
					cartProductRepo.save(existingCartProduct); // Save the updated existingCartProduct
				} else {
					// If the product is not in the cart, add it with the supplied quantity
					cartProduct.setCart(cart);
					cart.getCartProducts().add(cartProduct);
					cart.updateTotalAmount(product.getPrice() * cartProduct.getQuantity());
					cartProductRepo.save(cartProduct); // Save the new cartProduct
				}
			}

			// Save the updated cart in the repository
			cartRepo.save(cart);

			return ResponseEntity.ok(cart);
		} else {
			// Handle case where user doesn't have a cart (possibly return an error
			// response)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@DeleteMapping("/cart")
	public ResponseEntity<Object> deleteCart(@RequestHeader("JWT") String token, @RequestBody Product product) {
		User user = jwtUtil.getUser(token);
		// Retrieve the user's cart
		Optional<Cart> optionalCart = cartRepo.findByUserUsername(user.getUsername());

		if (optionalCart.isPresent()) {
			Cart cart = optionalCart.get();

			// Check if the product is in the cart
			Optional<CartProduct> optionalCartProduct = cartProductRepo
					.findByCartUserUserIdAndProductProductId(cart.getUser().getUserId(), product.getProductId());

			if (optionalCartProduct.isPresent()) {
				CartProduct cartProduct = optionalCartProduct.get();

				// Remove the product from the cart
				cart.getCartProducts().remove(cartProduct);
				cartProductRepo.delete(cartProduct);
				cart.updateTotalAmount(-cartProduct.getProduct().getPrice() * cartProduct.getQuantity());

				// Save the updated cart in the repository
				cartRepo.save(cart);

				return ResponseEntity.ok().build();
			} else {
				// Handle case where the product is not found in the cart (possibly return an
				// error response)
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} else {
			// Handle case where user doesn't have a cart (possibly return an error
			// response)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
}
