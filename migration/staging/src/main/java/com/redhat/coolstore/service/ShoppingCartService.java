package com.redhat.coolstore.service;

import com.redhat.coolstore.model.Product;
import com.redhat.coolstore.model.ShoppingCart;

public interface ShoppingCartService {
    ShoppingCart getShoppingCart(String cartId);

    Product getProduct(String itemId);

    ShoppingCart deleteItem(String cartId, String itemId, int quantity);

    ShoppingCart checkout(String cartId);

    ShoppingCart addItem(String cartId, String itemId, int quantity);

    ShoppingCart set(String cartId, String tmpId);

    void priceShoppingCart(ShoppingCart sc);
}
