package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProductTest {

    private static final long LEGACY_SERIAL_VERSION_UID = -7304814269819778382L;

    @Test
    void defaultConstructorCreatesEmptyProduct() {
        Product p = new Product();
        assertNull(p.getItemId());
        assertNull(p.getName());
        assertNull(p.getDesc());
        assertEquals(0.0, p.getPrice(), 0);
    }

    @Test
    void parameterizedConstructorSetsAllFields() {
        Product p = new Product("1111", "Car", "Super car", 1000);
        assertEquals("1111", p.getItemId());
        assertEquals("Car", p.getName());
        assertEquals("Super car", p.getDesc());
        assertEquals(1000, p.getPrice(), 0);
    }

    @Test
    void settersUpdateFields() {
        Product p = new Product();
        p.setItemId("2222");
        p.setName("Bike");
        p.setDesc("Super bike");
        p.setPrice(200);

        assertEquals("2222", p.getItemId());
        assertEquals("Bike", p.getName());
        assertEquals("Super bike", p.getDesc());
        assertEquals(200, p.getPrice(), 0);
    }

    @Test
    void serialVersionUidMatchesLegacy() throws NoSuchFieldException, IllegalAccessException {
        var field = Product.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long uid = field.getLong(null);
        assertEquals(LEGACY_SERIAL_VERSION_UID, uid);
    }

    @Test
    void serializationRoundTripPreservesFields() throws Exception {
        Product original = new Product("1111", "Car", "Super car", 1000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            Product deserialized = (Product) ois.readObject();
            assertEquals("1111", deserialized.getItemId());
            assertEquals("Car", deserialized.getName());
            assertEquals("Super car", deserialized.getDesc());
            assertEquals(1000, deserialized.getPrice(), 0);
        }
    }

    @Test
    void toStringContainsAllFields() {
        Product p = new Product("1111", "Car", "Super car", 1000);
        String s = p.toString();
        assertTrue(s.contains("1111"));
        assertTrue(s.contains("Car"));
        assertTrue(s.contains("Super car"));
        assertTrue(s.contains("1000"));
    }

    @Test
    void legacyObjectMotherProductValues() {
        List<Product> products = List.of(
            new Product("1111", "Car", "Super car", 1000),
            new Product("2222", "Bike", "Super bike", 200));

        assertEquals(2, products.size());
        assertEquals("1111", products.get(0).getItemId());
        assertEquals("Car", products.get(0).getName());
        assertEquals("Super car", products.get(0).getDesc());
        assertEquals(1000, products.get(0).getPrice(), 0);

        assertEquals("2222", products.get(1).getItemId());
        assertEquals("Bike", products.get(1).getName());
        assertEquals("Super bike", products.get(1).getDesc());
        assertEquals(200, products.get(1).getPrice(), 0);
    }
}
