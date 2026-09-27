package com.petshop.repository;

import com.petshop.model.Product;
import java.util.List;
import java.util.Set;

public interface WishlistRepositoryCustom {

    List<Product> getWishlistProductsByUserId(int userId);

    Set<Integer> getWishlistProductIdsByUserId(int userId);

    boolean isInWishlist(int userId, int productId);

    boolean addToWishlist(int userId, int productId);

    boolean removeFromWishlist(int userId, int productId);

    boolean toggleWishlist(int userId, int productId);

    boolean toggleWishlistAndReturnState(int userId, int productId) throws java.sql.SQLException;
}
