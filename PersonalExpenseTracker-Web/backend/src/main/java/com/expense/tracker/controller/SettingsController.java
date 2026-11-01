package com.expense.tracker.controller;

import com.expense.tracker.dto.UserSettingsDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.service.SettingsService;
import com.expense.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<UserSettingsDTO> getSettings() {
        User user = userService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(settingsService.getSettings(user));
    }

    @PutMapping
    public ResponseEntity<UserSettingsDTO> updateSettings(@Valid @RequestBody UserSettingsDTO dto) {
        User user = userService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(settingsService.updateSettings(user, dto));
    }
}
