package com.caerus.audit.server.util;

import java.util.Set;
import java.util.regex.Pattern;

public class PatternValidator {
    private static final Set<String> ALLOWED_TOKENS = Set.of(
            "HOSTNAME", "YEAR", "MONTH", "DAY", "HOUR",
            "MIN", "SECOND", "DATE", "TIMESTAMP", "EXT"
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([^}]+)}");

    public static void validate(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("Folder structure template cannot be empty");
        }

        var matcher = TOKEN_PATTERN.matcher(pattern);

        while (matcher.find()) {
            String token = matcher.group(1).toUpperCase();
            if (!ALLOWED_TOKENS.contains(token)) {
                throw new IllegalArgumentException("Invalid token in folder pattern: {" + token + "}");
            }
        }
    }
}
