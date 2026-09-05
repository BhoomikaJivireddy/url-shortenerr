package com.susu.urlshortener.util;

public class UserAgentParser {

    public static String getBrowser(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("PostmanRuntime")) {
            return "Postman";
        }

        if (userAgent.contains("Edg/")) {
            return "Edge";
        }

        if (userAgent.contains("Chrome/")) {
            return "Chrome";
        }

        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }

        if (userAgent.contains("Safari/")
                && !userAgent.contains("Chrome/")) {
            return "Safari";
        }

        return "Other";
    }

    public static String getOperatingSystem(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Windows")) {
            return "Windows";
        }

        if (userAgent.contains("Android")) {
            return "Android";
        }

        if (userAgent.contains("iPhone")
                || userAgent.contains("iPad")) {
            return "iOS";
        }

        if (userAgent.contains("Mac OS X")) {
            return "macOS";
        }

        if (userAgent.contains("Linux")) {
            return "Linux";
        }

        return "Other";
    }

    public static String getDevice(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("iPhone")
                || userAgent.contains("Android")) {
            return "Mobile";
        }

        if (userAgent.contains("iPad")) {
            return "Tablet";
        }

        return "Desktop";
    }
}