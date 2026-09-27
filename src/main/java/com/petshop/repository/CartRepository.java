package com.petshop.repository;

import com.petshop.model.CartRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartRow, Integer>, CartRepositoryCustom {
}
