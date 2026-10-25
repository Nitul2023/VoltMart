package com.voltmart.web.store;

import com.voltmart.dto.CommerceDtos.CartItemRequest;
import com.voltmart.dto.CommerceDtos.CartView;
import com.voltmart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartView get(@RequestHeader(value = "X-Cart-Session", required = false) String sessionId) {
        return cartService.getOrCreate(sessionId);
    }

    @PostMapping("/items")
    public CartView add(@RequestHeader(value = "X-Cart-Session", required = false) String sessionId,
                        @Valid @RequestBody CartItemRequest request) {
        return cartService.add(sessionId, request);
    }

    @PatchMapping("/items/{itemId}")
    public CartView update(@RequestHeader(value = "X-Cart-Session", required = false) String sessionId,
                           @PathVariable UUID itemId,
                           @RequestBody Map<String, Integer> body) {
        Integer qty = body.get("quantity");
        return cartService.update(sessionId, itemId, qty == null ? 0 : qty);
    }

    @DeleteMapping
    public CartView clear(@RequestHeader(value = "X-Cart-Session", required = false) String sessionId) {
        return cartService.clear(sessionId);
    }
}
