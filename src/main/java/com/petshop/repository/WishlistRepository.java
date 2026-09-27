package com.petshop.repository;

import com.petshop.model.WishlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<WishlistEntry, Integer>, WishlistRepositoryCustom {
}
