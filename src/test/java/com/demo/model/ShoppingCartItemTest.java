package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class ShoppingCartItemTest {

    private static final long LEGACY_SERIAL_VERSION_UID = 6964558044240061049L;

    @Test
    void defaultConstructorCreatesEmptyItem() {
        ShoppingCartItem item = new ShoppingCartItem();
        assertEquals(0.0, item.getPrice(), 0);
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getPromoSavings(), 0);
        assertNull(item.getProduct());
    }

    @Test
    void settersSetAllFields() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product("1111", "Car", "Super car", 1000);
        item.setPrice(1000);
        item.setQuantity(2);
        item.setPromoSavings(100);
        item.setProduct(product);

        assertEquals(1000, item.getPrice(), 0);
        assertEquals(2, item.getQuantity());
        assertEquals(100, item.getPromoSavings(), 0);
        assertEquals(product, item.getProduct());
    }

    @Test
    void serialVersionUidMatchesLegacy() throws NoSuchFieldException, IllegalAccessException {
        var field = ShoppingCartItem.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long uid = field.getLong(null);
        assertEquals(LEGACY_SERIAL_VERSION_UID, uid);
    }

    @Test
    void serializationRoundTripPreservesFields() throws Exception {
        Product product = new Product("1111", "Car", "Super car", 1000);
        ShoppingCartItem original = new ShoppingCartItem();
        original.setPrice(1000);
        original.setQuantity(2);
        original.setPromoSavings(100);
        original.setProduct(product);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ShoppingCartItem deserialized = (ShoppingCartItem) ois.readObject();
            assertEquals(1000, deserialized.getPrice(), 0);
            assertEquals(2, deserialized.getQuantity());
            assertEquals(100, deserialized.getPromoSavings(), 0);
            assertNotNull(deserialized.getProduct());
            assertEquals("1111", deserialized.getProduct().getItemId());
        }
    }

    @Test
    void toStringContainsAllFields() {
        Product product = new Product("1111", "Car", "Super car", 1000);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(1000);
        item.setQuantity(2);
        item.setPromoSavings(100);
        item.setProduct(product);

        String s = item.toString();
        assertTrue(s.contains("1000"));
        assertTrue(s.contains("2"));
        assertTrue(s.contains("100"));
    }

    @Test
    void productReferenceIsMutable() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product p1 = new Product("1111", "Car", "Super car", 1000);
        Product p2 = new Product("2222", "Bike", "Super bike", 200);
        item.setProduct(p1);
        assertEquals(p1, item.getProduct());
        item.setProduct(p2);
        assertEquals(p2, item.getProduct());
    }
}
