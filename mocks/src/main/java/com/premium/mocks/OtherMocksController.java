package com.premium.mocks;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class OtherMocksController {
    private final Chaos chaos;
    private final List<Map<String, Object>> sent = Collections.synchronizedList(new ArrayList<>());

    public OtherMocksController(Chaos chaos) {
        this.chaos = chaos;
    }

    @PostMapping("/kyc/check")
    public Map<String, Object> kyc(@RequestBody Map<String, Object> body) {
        chaos.apply("kyc");
        boolean fail = "POL-KYCFAIL".equals(String.valueOf(body.get("policyNumber")));
        return Map.of("status", fail ? "FAIL" : "PASS");
    }

    @PostMapping("/fraud/score")
    public Map<String, Object> fraud(@RequestBody Map<String, Object> body) {
        chaos.apply("fraud");
        double amount = Double.parseDouble(String.valueOf(body.get("amount")));
        int score = amount >= 50000 ? 85 : 10;
        return Map.of("paymentRef", String.valueOf(body.get("paymentRef")), "score", score);
    }

    @PostMapping("/notify/send")
    public Map<String, Object> notifyPolicy(@RequestBody Map<String, Object> body) {
        chaos.apply("notify");
        sent.add(body);
        return Map.of("status", "SENT");
    }

    @GetMapping("/notify/sent")
    public Map<String, Object> sentList() {
        return Map.of("count", sent.size(), "items", new ArrayList<>(sent));
    }
}