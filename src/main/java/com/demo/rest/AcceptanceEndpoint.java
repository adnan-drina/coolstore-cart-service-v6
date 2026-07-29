package com.demo.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@ApplicationScoped
@Path("/api/cart")
public class AcceptanceEndpoint {

    @GET
    @Path("/acceptance-check")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> acceptanceCheck() {
        return Map.of(
            "status", "service_interfaces_ready",
            "story", "S02"
        );
    }
}
