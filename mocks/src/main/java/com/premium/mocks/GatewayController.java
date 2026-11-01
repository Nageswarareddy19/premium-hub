package com.premium.mocks;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/gateway")
public class GatewayController {
    private final Chaos chaos;
    private final RestClient rest = RestClient.create();
    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

    @Value("${mock.webhook.url:}")
    private String webhookUrl;
    @Value("${mock.webhook.secret:dev-secret}")
    private String secret;
    @Value("${mock.webhook.delay-ms:2000}")
    private long delayMs;

    public GatewayController(Chaos chaos) {
        this.chaos = chaos;
    }

    @PostMapping("/charge")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> charge(@RequestBody Map<String, Object> body) {
        chaos.apply("gateway");
        String ref = String.valueOf(body.get("paymentRef"));
        double amount = Double.parseDouble(String.valueOf(body.get("amount")));
        String gatewayRef = "GW-" + UUID.randomUUID().toString().substring(0, 8);
        String finalStatus = amount >= 100000 ? "FAILED" : "SUCCESS";
        if (!webhookUrl.isBlank()) {
            pool.schedule(() -> sendWebhook(ref, finalStatus, gatewayRef), delayMs, TimeUnit.MILLISECONDS);
        }
        return Map.of("paymentRef", ref, "gatewayRef", gatewayRef, "status", "PENDING");
    }

    // Manual trigger, useful for testing the webhook by hand.
    @PostMapping("/webhook/send")
    public Map<String, Object> manual(@RequestParam String paymentRef,
                                      @RequestParam(defaultValue = "SUCCESS") String status) {
        sendWebhook(paymentRef, status, "GW-MANUAL");
        return Map.of("sent", true);
    }

    private void sendWebhook(String ref, String status, String gatewayRef) {
        try {
            String json = "{\"paymentRef\":\"" + ref + "\",\"status\":\"" + status
                + "\",\"gatewayRef\":\"" + gatewayRef + "\"}";
            rest.post().uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", hmac(json))
                .body(json)
                .retrieve().toBodilessEntity();
        } catch (Exception e) {
            System.err.println("webhook failed: " + e.getMessage());
        }
    }

    private String hmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}