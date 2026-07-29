package com.demo.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.demo.model.Product;

@RegisterRestClient
@Path("/api")
interface CatalogService {
    @GET
    @Path("/products")
    List<Product> products();
}
