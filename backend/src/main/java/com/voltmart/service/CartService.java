package com.voltmart.service;

import com.voltmart.domain.entity.Cart;
import com.voltmart.domain.entity.CartItem;
import com.voltmart.domain.entity.ProductVariant;
import com.voltmart.domain.entity.User;
import com.voltmart.dto.CommerceDtos.CartItemRequest;
import com.voltmart.dto.CommerceDtos.CartItemView;
import com.voltmart.dto.CommerceDtos.CartView;
import com.voltmart.exception.ApiException;
import com.voltmart.repository.CartRepository;
import com.voltmart.repository.ProductVariantRepository;
import com.voltmart.repository.UserRepository;
import com.voltmart.util.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductVariantRepository variantRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartView getOrCreate(String sessionId) {
        return toView(resolve(sessionId));
    }

    @Transactional
    public CartView add(String sessionId, CartItemRequest request) {
        Cart cart = resolve(sessionId);
        ProductVariant variant = variantRepository.findById(request.variantId())
                .orElseThrow(() -> ApiException.notFound("Variant not found"));
        if (!variant.isActive() || !variant.getProduct().isActive()) {
            throw ApiException.badRequest("Product is unavailable");
        }
        if (request.quantity() > variant.availableQty()) {
            throw ApiException.badRequest("Insufficient stock for " + variant.getSku());
        }
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(variant.getId()))
                .findFirst().orElse(null);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(request.quantity());
            cart.getItems().add(item);
        } else {
            int next = existing.getQuantity() + request.quantity();
            if (next > variant.availableQty()) {
                throw ApiException.badRequest("Insufficient stock for " + variant.getSku());
            }
            existing.setQuantity(next);
        }
        cart.setUpdatedAt(Instant.now());
        return toView(cartRepository.save(cart));
    }

    @Transactional
    public CartView update(String sessionId, UUID itemId, int quantity) {
        Cart cart = resolve(sessionId);
        CartItem item = cart.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Cart item not found"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (quantity > item.getVariant().availableQty()) {
                throw ApiException.badRequest("Insufficient stock");
            }
            item.setQuantity(quantity);
        }
        cart.setUpdatedAt(Instant.now());
        return toView(cartRepository.save(cart));
    }

    @Transactional
    public CartView clear(String sessionId) {
        Cart cart = resolve(sessionId);
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        return toView(cartRepository.save(cart));
    }

    @Transactional
    public Cart requireUserCart() {
        UUID userId = CurrentUser.requireId();
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.badRequest("Cart is empty"));
    }

    private Cart resolve(String sessionId) {
        return CurrentUser.principal()
                .map(p -> cartRepository.findByUserId(p.getId()).orElseGet(() -> {
                    Cart cart = new Cart();
                    User user = userRepository.getReferenceById(p.getId());
                    cart.setUser(user);
                    return cartRepository.save(cart);
                }))
                .orElseGet(() -> {
                    if (sessionId == null || sessionId.isBlank()) {
                        throw ApiException.badRequest("Missing cart session");
                    }
                    return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setSessionId(sessionId);
                        return cartRepository.save(cart);
                    });
                });
    }

    public CartView toView(Cart cart) {
        List<CartItem> source = cart.getItems() == null ? List.of() : cart.getItems();
        List<CartItemView> items = source.stream().map(this::toItem).toList();
        BigDecimal subtotal = items.stream().map(CartItemView::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = items.stream().mapToInt(CartItemView::quantity).sum();
        return new CartView(cart.getId(), items, subtotal, count);
    }

    private CartItemView toItem(CartItem item) {
        ProductVariant v = item.getVariant();
        String image = v.getProduct().getImages().isEmpty() ? null : v.getProduct().getImages().get(0).getUrl();
        BigDecimal line = v.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemView(item.getId(), v.getId(), v.getProduct().getId(), v.getProduct().getName(),
                v.getName(), v.getSku(), image, v.getPrice(), item.getQuantity(), v.availableQty(), line);
    }
}
