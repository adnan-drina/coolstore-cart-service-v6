package com.demo.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.demo.model.Promotion;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

@ApplicationScoped
public class PromoService implements Serializable {

    private static final long serialVersionUID = 2088590587856645568L;

    private String name = null;

    private Set<Promotion> promotionSet = Collections.synchronizedSet(new HashSet<>());

    public PromoService() {
        // Coolstore seed item also used by inventory/catalog demos
        promotionSet.add(new Promotion("329299", .25));
    }

    public void applyCartItemPromotions(ShoppingCart shoppingCart) {
        if (shoppingCart != null && shoppingCart.getShoppingCartItemList().size() > 0) {
            Map<String, Promotion> promoMap = new HashMap<String, Promotion>();
            for (Promotion promo : getPromotions()) {
                promoMap.put(promo.getItemId(), promo);
            }

            for (ShoppingCartItem sci : shoppingCart.getShoppingCartItemList()) {
                String productId = sci.getProduct().getItemId();
                Promotion promo = promoMap.get(productId);
                if (promo != null) {
                    sci.setPromoSavings(sci.getProduct().getPrice() * promo.getPercentOff() * -1);
                    sci.setPrice(sci.getProduct().getPrice() * (1 - promo.getPercentOff()));
                }
            }
        }
    }

    public void applyShippingPromotions(ShoppingCart shoppingCart) {
        if (shoppingCart != null) {
            // PROMO: if cart total is greater than 75, free shipping
            if (shoppingCart.getCartItemTotal() >= 75) {
                shoppingCart.setShippingPromoSavings(shoppingCart.getShippingTotal() * -1);
                shoppingCart.setShippingTotal(0);
            }
        }
    }

    public Set<Promotion> getPromotions() {
        return new HashSet<>(promotionSet);
    }

    public void setPromotions(Set<Promotion> promotionSet) {
        this.promotionSet.clear();
        if (promotionSet != null) {
            this.promotionSet.addAll(promotionSet);
        }
    }

    @Override
    public String toString() {
        return "PromoService [name=" + name + ", promotionSet=" + promotionSet + "]";
    }
}
