package com.ecom.ecommerce.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.ecommerce.models.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer>{
	Optional<Cart> findByUserUsername(String userName);
}