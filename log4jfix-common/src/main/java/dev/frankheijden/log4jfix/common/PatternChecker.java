package dev.frankheijden.log4jfix.common;

import java.util.regex.Pattern;

public class PatternChecker {

    private static final Pattern EXPLOIT_PATTERN = Pattern.compile("\\$\\{(jndi|log4j|sys|env|main|marker|java|base64|lower|upper|web|docker|kubernetes|spring|jvmrunargs|date|ctx)\\:.*\\}", Pattern.CASE_INSENSITIVE);

    private PatternChecker() {}

    public static boolean isExploit(String message) {
        if (message == null) return false;
        return EXPLOIT_PATTERN.matcher(message).find();
    }
}
