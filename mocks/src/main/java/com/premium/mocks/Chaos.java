package com.premium.mocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class Chaos {
    public enum Mode { OK, DOWN, SLOW }

    private final Map<String, Mode> modes = new ConcurrentHashMap<>();
    private final Map<String, Long> delays = new ConcurrentHashMap<>();

    public void set(String service, Mode mode, long delayMs) {
        modes.put(service, mode);
        delays.put(service, delayMs);
    }

    public Map<String, Object> status() {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String s : new String[] {"policy", "gateway", "kyc", "fraud", "notify"}) {
            out.put(s, modes.getOrDefault(s, Mode.OK));
        }
        return out;
    }

    // Every mock endpoint calls this first.
    public void apply(String service) {
        Mode mode = modes.getOrDefault(service, Mode.OK);
        if (mode == Mode.DOWN) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, service + " is down");
        }
        if (mode == Mode.SLOW) {
            try {
                Thread.sleep(delays.getOrDefault(service, 5000L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}