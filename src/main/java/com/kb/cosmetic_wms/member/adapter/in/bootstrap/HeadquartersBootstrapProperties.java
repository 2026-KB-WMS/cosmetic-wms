package com.kb.cosmetic_wms.member.adapter.in.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "bootstrap.headquarters")
public record HeadquartersBootstrapProperties(
        String loginId,
        String password,
        String memberName,
        String email,
        String phoneNumber
) {

    private static final String PREFIX = "bootstrap.headquarters.";

    public boolean isUnset() {
        return missingKeys().size() == 5;
    }

    public List<String> missingKeys() {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, loginId, "login-id");
        addIfBlank(missing, password, "password");
        addIfBlank(missing, memberName, "member-name");
        addIfBlank(missing, email, "email");
        addIfBlank(missing, phoneNumber, "phone-number");
        return missing;
    }

    private static void addIfBlank(List<String> missing, String value, String key) {
        if (value == null || value.isBlank()) {
            missing.add(PREFIX + key);
        }
    }
}
