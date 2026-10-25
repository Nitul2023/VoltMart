package com.voltmart.web.store;

import com.voltmart.dto.AuthDtos.UserView;
import com.voltmart.dto.CatalogDtos.ProductCard;
import com.voltmart.dto.CommerceDtos.AddressRequest;
import com.voltmart.dto.CommerceDtos.AddressView;
import com.voltmart.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public UserView me() {
        return accountService.me();
    }

    @PatchMapping
    public UserView update(@RequestBody Map<String, String> body) {
        return accountService.updateProfile(body.get("firstName"), body.get("lastName"), body.get("phone"));
    }

    @GetMapping("/addresses")
    public List<AddressView> addresses() {
        return accountService.addresses();
    }

    @PostMapping("/addresses")
    public AddressView addAddress(@Valid @RequestBody AddressRequest request) {
        return accountService.addAddress(request);
    }

    @GetMapping("/wishlist")
    public List<ProductCard> wishlist() {
        return accountService.wishlist();
    }

    @PostMapping("/wishlist/{productId}")
    public void addWishlist(@PathVariable UUID productId) {
        accountService.addWishlist(productId);
    }

    @DeleteMapping("/wishlist/{productId}")
    public void removeWishlist(@PathVariable UUID productId) {
        accountService.removeWishlist(productId);
    }
}
