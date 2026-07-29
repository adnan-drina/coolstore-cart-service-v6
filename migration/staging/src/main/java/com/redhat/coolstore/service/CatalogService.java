package com.redhat.coolstore.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.redhat.coolstore.model.Product;

@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
