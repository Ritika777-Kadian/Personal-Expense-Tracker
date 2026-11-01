package com.expense.tracker.controller;

import com.expense.tracker.model.User;
import com.expense.tracker.service.SampleDataService;
import com.expense.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sample-data")
public class SampleDataController {

    @Autowired
    private UserService userService;

    @Autowired
    private SampleDataService sampleDataService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSampleDataStatus() {
        User user = userService.getCurrentAuthenticatedUser();
        boolean hasData = sampleDataService.hasSampleData(user);
        long count = sampleDataService.getSampleDataCount(user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("hasSampleData", hasData);
        resp.put("sampleCount", count);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedSampleData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        User user = userService.getCurrentAuthenticatedUser();
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        int seeded = sampleDataService.seedSampleData(user, targetYear, targetMonth);

        Map<String, Object> resp = new HashMap<>();
        if (seeded > 0) {
            resp.put("success", true);
            resp.put("count", seeded);
            resp.put("message", "Successfully loaded " + seeded + " realistic sample demo transactions and budgets across multiple months.");
        } else {
            resp.put("success", false);
            resp.put("count", 0);
            resp.put("message", "Demo data is already active in your account. To reload, please clear existing demo data first.");
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/purge")
    public ResponseEntity<Map<String, Object>> purgeSampleData() {
        User user = userService.getCurrentAuthenticatedUser();
        int removed = sampleDataService.purgeSampleData(user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("removed", removed);
        resp.put("message", "Removed " + removed + " demo records. All personal user transactions were preserved.");
        return ResponseEntity.ok(resp);
    }
}
