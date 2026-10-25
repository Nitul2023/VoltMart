package com.voltmart.service;

import com.voltmart.domain.entity.Address;
import com.voltmart.domain.entity.Product;
import com.voltmart.domain.entity.Review;
import com.voltmart.domain.entity.User;
import com.voltmart.domain.entity.WishlistItem;
import com.voltmart.dto.AuthDtos.UserView;
import com.voltmart.dto.CatalogDtos.ProductCard;
import com.voltmart.dto.CommerceDtos.AddressRequest;
import com.voltmart.dto.CommerceDtos.AddressView;
import com.voltmart.dto.CommerceDtos.ReviewRequest;
import com.voltmart.exception.ApiException;
import com.voltmart.mapper.ProductMapper;
import com.voltmart.repository.AddressRepository;
import com.voltmart.repository.ProductRepository;
import com.voltmart.repository.ReviewRepository;
import com.voltmart.repository.UserRepository;
import com.voltmart.repository.WishlistRepository;
import com.voltmart.util.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final AuthService authService;

    public AccountService(UserRepository userRepository, AddressRepository addressRepository,
                          WishlistRepository wishlistRepository, ProductRepository productRepository,
                          ReviewRepository reviewRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserView me() {
        return authService.toView(user());
    }

    @Transactional
    public UserView updateProfile(String firstName, String lastName, String phone) {
        User user = user();
        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank()) user.setLastName(lastName);
        if (phone != null) user.setPhone(phone);
        return authService.toView(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AddressView> addresses() {
        return addressRepository.findByUserId(CurrentUser.requireId()).stream().map(this::toAddress).toList();
    }

    @Transactional
    public AddressView addAddress(AddressRequest request) {
        Address address = new Address();
        address.setUser(user());
        address.setLabel(request.label());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country() == null ? "United States" : request.country());
        address.setDefaultAddress(request.defaultAddress());
        return toAddress(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<ProductCard> wishlist() {
        return wishlistRepository.findByUserId(CurrentUser.requireId()).stream()
                .map(WishlistItem::getProduct)
                .map(ProductMapper::toCard)
                .toList();
    }

    @Transactional
    public void addWishlist(UUID productId) {
        if (wishlistRepository.existsByUserIdAndProductId(CurrentUser.requireId(), productId)) {
            return;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        WishlistItem item = new WishlistItem();
        item.setUserId(CurrentUser.requireId());
        item.setProductId(product.getId());
        wishlistRepository.save(item);
    }

    @Transactional
    public void removeWishlist(UUID productId) {
        wishlistRepository.deleteByUserIdAndProductId(CurrentUser.requireId(), productId);
    }

    @Transactional
    public void addReview(UUID productId, ReviewRequest request) {
        UUID userId = CurrentUser.requireId();
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw ApiException.conflict("You already reviewed this product");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user());
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setBody(request.body());
        review.setVerified(true);
        reviewRepository.save(review);
    }

    private User user() {
        return userRepository.findById(CurrentUser.requireId())
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
    }

    private AddressView toAddress(Address a) {
        return new AddressView(a.getId(), a.getLabel(), a.getLine1(), a.getLine2(), a.getCity(),
                a.getState(), a.getPostalCode(), a.getCountry(), a.isDefaultAddress());
    }
}
