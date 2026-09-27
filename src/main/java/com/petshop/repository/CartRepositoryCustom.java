package com.petshop.repository;

import com.petshop.model.CartItem;
import java.util.Map;

public interface CartRepositoryCustom {

    void saveCartItem(int userId, int productId, int quantity);

    void addToCart(int userId, int productId, int quantityToAdd);

    void removeFromCart(int userId, int productId);

    boolean updateCartQuantity(int userId, int productId, int newQuantity);

    void clearCart(int userId);

    Map<Integer, CartItem> getCartByUserId(int userId);

    int getTotalQuantity(int userId);

    void syncCartFromSession(int userId, Map<Integer, CartItem> sessionCart);
}
