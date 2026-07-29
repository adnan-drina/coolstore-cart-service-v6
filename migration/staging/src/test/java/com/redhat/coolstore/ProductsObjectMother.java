package com.redhat.coolstore;

import com.redhat.coolstore.model.Product;
import java.util.Arrays;
import java.util.List;

public class ProductsObjectMother {

    public static List<Product> createVehicleProducts() {
        return Arrays.asList(
            new Product("1111", "Car", "Super car", 1000),
            new Product("2222", "Bike", "Super bike", 200));
    }
}
