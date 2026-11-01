package com.premium.mocks;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final Chaos chaos;

    public AdminController(Chaos chaos) {
        this.chaos = chaos;
    }

    @PostMapping("/chaos/{service}")
    public Map<String, Object> set(@PathVariable String service,
                                   @RequestParam Chaos.Mode mode,
                                   @RequestParam(defaultValue = "5000") long delayMs) {
        chaos.set(service, mode, delayMs);
        return chaos.status();
    }

    @GetMapping("/chaos")
    public Map<String, Object> get() {
        return chaos.status();
    }
}