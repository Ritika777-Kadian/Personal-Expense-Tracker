package com.expense.tracker.service;

import com.expense.tracker.config.JwtTokenProvider;
import com.expense.tracker.dto.AuthRequest;
import com.expense.tracker.dto.AuthResponse;
import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.exception.DuplicateResourceException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.exception.ValidationException;
import com.expense.tracker.model.User;
import com.expense.tracker.model.UserSettings;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository settingsRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName() != null ? request.getFullName().trim() : request.getUsername().trim()
        );

        User savedUser = userRepository.save(user);

        // Create default settings
        UserSettings settings = new UserSettings(savedUser);
        settingsRepository.save(settings);

        // Initialize default categories for new user
        categoryService.initializeDefaultCategories(savedUser);

        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getUsername());
        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(),
                savedUser.getFullName(), settings.getCurrency(), settings.getCurrencySymbol());
    }

    public AuthResponse login(AuthRequest request) {
        String identifier = request.getUsername().trim();
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> new ValidationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid username or password");
        }

        UserSettings settings = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> settingsRepository.save(new UserSettings(user)));

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(),
                user.getFullName(), settings.getCurrency(), settings.getCurrencySymbol());
    }

    public User getCurrentAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ValidationException("User is not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
