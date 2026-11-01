package com.premium.mocks;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/policy")
public class PolicyController {
	private final Chaos chaos;
	private final Map<String, Map<String, Object>> policies = new LinkedHashMap<>();
	private final List<String> received = Collections.synchronizedList(new ArrayList<>());
	private final Set<String> unique = ConcurrentHashMap.newKeySet();

	public PolicyController(Chaos chaos) {
		this.chaos = chaos;
		policies.put("POL-5001", Map.of("policyNumber", "POL-5001", "holderName", "Asha Rao", "status", "ACTIVE",
				"expiryDate", LocalDate.now().plusDays(20).toString(), "premiumAmount", 1250.0));
		policies.put("POL-5002", Map.of("policyNumber", "POL-5002", "holderName", "Ravi Kumar", "status", "ACTIVE",
				"expiryDate", LocalDate.now().plusDays(10).toString(), "premiumAmount", 3400.0));
		policies.put("POL-5003", Map.of("policyNumber", "POL-5003", "holderName", "Meena Iyer", "status", "ACTIVE",
				"expiryDate", LocalDate.now().plusDays(200).toString(), "premiumAmount", 980.0));
	}

	@GetMapping("/policies/{no}")
	public Map<String, Object> get(@PathVariable String no) {
		chaos.apply("policy");
		Map<String, Object> p = policies.get(no);
		if (p == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown policy");
		}
		return p;
	}

	@PostMapping("/policy-payments")
	public Map<String, Object> pay(@RequestBody Map<String, Object> body) {
		chaos.apply("policy");
		String ref = String.valueOf(body.get("paymentRef"));
		received.add(ref); // every arrival, including duplicates
		boolean first = unique.add(ref);
		return Map.of("paymentRef", ref, "status", first ? "UPDATED" : "DUPLICATE_IGNORED");
	}

	@GetMapping("/policy-payments/received")
	public Map<String, Object> received() {
		return Map.of("count", received.size(), "unique", unique.size(), "refs", new ArrayList<>(received));
	}
}