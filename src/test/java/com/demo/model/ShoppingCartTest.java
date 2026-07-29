package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ShoppingCartTest {

    private static final long LEGACY_SERIAL_VERSION_UID = -1108043957592113528L;

    @Test
    void defaultConstructorCreatesEmptyCart() {
        ShoppingCart cart = new ShoppingCart();
        assertNull(cart.getCartId());
        assertEquals(0.0, cart.getCartItemTotal(), 0);
        assertEquals(0.0, cart.getCartItemPromoSavings(), 0);
        assertEquals(0.0, cart.getShippingTotal(), 0);
        assertEquals(0.0, cart.getShippingPromoSavings(), 0);
        assertEquals(0.0, cart.getCartTotal(), 0);
        assertNotNull(cart.getShoppingCartItemList());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void parameterizedConstructorSetsCartId() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        assertEquals("CART-001", cart.getCartId());
        assertNotNull(cart.getShoppingCartItemList());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void settersUpdateAllFields() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartId("CART-001");
        cart.setCartItemTotal(500);
        cart.setCartItemPromoSavings(50);
        cart.setShippingTotal(10);
        cart.setShippingPromoSavings(2);
        cart.setCartTotal(458);

        assertEquals("CART-001", cart.getCartId());
        assertEquals(500, cart.getCartItemTotal(), 0);
        assertEquals(50, cart.getCartItemPromoSavings(), 0);
        assertEquals(10, cart.getShippingTotal(), 0);
        assertEquals(2, cart.getShippingPromoSavings(), 0);
        assertEquals(458, cart.getCartTotal(), 0);
    }

    @Test
    void serialVersionUidMatchesLegacy() throws NoSuchFieldException, IllegalAccessException {
        var field = ShoppingCart.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long uid = field.getLong(null);
        assertEquals(LEGACY_SERIAL_VERSION_UID, uid);
    }

    @Test
    void addShoppingCartItemAddsNonNullItem() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100);
        item.setQuantity(1);

        cart.addShoppingCartItem(item);
        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void addShoppingCartItemIgnoresNull() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        cart.addShoppingCartItem(null);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void addShoppingCartItemAddsMultipleItems() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(100);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(200);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);
        assertEquals(2, cart.getShoppingCartItemList().size());
    }

    @Test
    void removeShoppingCartItemRemovesMatchingItem() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100);
        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(item);
        assertTrue(removed);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void removeShoppingCartItemReturnsFalseForNull() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        boolean removed = cart.removeShoppingCartItem(null);
        assertFalse(removed);
    }

    @Test
    void removeShoppingCartItemReturnsFalseForAbsentItem() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(999);
        boolean removed = cart.removeShoppingCartItem(item);
        assertFalse(removed);
    }

    @Test
    void resetShoppingCartItemListClearsList() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100);
        cart.addShoppingCartItem(item);
        assertEquals(1, cart.getShoppingCartItemList().size());

        cart.resetShoppingCartItemList();
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void setShoppingCartItemListReplacesList() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100);
        cart.addShoppingCartItem(item);

        List<ShoppingCartItem> newList = new ArrayList<>();
        ShoppingCartItem newItem = new ShoppingCartItem();
        newItem.setPrice(500);
        newList.add(newItem);
        cart.setShoppingCartItemList(newList);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(500, cart.getShoppingCartItemList().get(0).getPrice(), 0);
    }

    @Test
    void serializationRoundTripPreservesFields() throws Exception {
        ShoppingCart original = new ShoppingCart("CART-001");
        original.setCartItemTotal(500);
        original.setCartTotal(458);

        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100);
        item.setQuantity(2);
        original.addShoppingCartItem(item);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ShoppingCart deserialized = (ShoppingCart) ois.readObject();
            assertEquals("CART-001", deserialized.getCartId());
            assertEquals(500, deserialized.getCartItemTotal(), 0);
            assertEquals(458, deserialized.getCartTotal(), 0);
            assertEquals(1, deserialized.getShoppingCartItemList().size());
        }
    }

    @Test
    void toStringContainsAllFields() {
        ShoppingCart cart = new ShoppingCart("CART-001");
        cart.setCartItemTotal(500);
        cart.setCartItemPromoSavings(50);
        cart.setShippingTotal(10);
        cart.setShippingPromoSavings(2);
        cart.setCartTotal(458);

        String s = cart.toString();
        assertTrue(s.contains("CART-001"));
        assertTrue(s.contains("500"));
        assertTrue(s.contains("50"));
        assertTrue(s.contains("10"));
        assertTrue(s.contains("2"));
        assertTrue(s.contains("458"));
    }

    @Test
    void shoppingCartItemListInitializedAsArrayList() {
        ShoppingCart cart = new ShoppingCart();
        assertInstanceOf(ArrayList.class, cart.getShoppingCartItemList());
    }
}
