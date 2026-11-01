package com.expense.tracker.service;

import com.expense.tracker.dto.UserSettingsDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.model.UserSettings;
import com.expense.tracker.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    @Autowired
    private UserSettingsRepository settingsRepository;

    public UserSettingsDTO getSettings(User user) {
        UserSettings s = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> settingsRepository.save(new UserSettings(user)));
        return new UserSettingsDTO(s.getCurrency(), s.getCurrencySymbol(), s.getDateFormat(), s.getTheme(), s.isFirstLaunchPromptShown());
    }

    @Transactional
    public UserSettingsDTO updateSettings(User user, UserSettingsDTO dto) {
        UserSettings s = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserSettings(user));

        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            s.setCurrency(dto.getCurrency().trim());
        }
        if (dto.getDateFormat() != null && !dto.getDateFormat().isBlank()) {
            s.setDateFormat(dto.getDateFormat().trim());
        }
        if (dto.getTheme() != null && !dto.getTheme().isBlank()) {
            s.setTheme(dto.getTheme().trim());
        }
        s.setFirstLaunchPromptShown(dto.isFirstLaunchPromptShown());

        UserSettings saved = settingsRepository.save(s);
        return new UserSettingsDTO(saved.getCurrency(), saved.getCurrencySymbol(), saved.getDateFormat(), saved.getTheme(), saved.isFirstLaunchPromptShown());
    }
}
