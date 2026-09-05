package com.susu.urlshortener.dto;

import java.util.Map;

public class AnalyticsResponse {

    private Long urlId;
    private String shortCode;
    private String originalUrl;

    private long totalClicks;
    private long clicksToday;
    private long clicksThisWeek;
    private long clicksThisMonth;

    private Map<String, Long> browsers;
    private Map<String, Long> operatingSystems;
    private Map<String, Long> devices;

    public AnalyticsResponse(
            Long urlId,
            String shortCode,
            String originalUrl,
            long totalClicks,
            long clicksToday,
            long clicksThisWeek,
            long clicksThisMonth,
            Map<String, Long> browsers,
            Map<String, Long> operatingSystems,
            Map<String, Long> devices
    ) {
        this.urlId = urlId;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.totalClicks = totalClicks;
        this.clicksToday = clicksToday;
        this.clicksThisWeek = clicksThisWeek;
        this.clicksThisMonth = clicksThisMonth;
        this.browsers = browsers;
        this.operatingSystems = operatingSystems;
        this.devices = devices;
    }

    public Long getUrlId() {
        return urlId;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public long getClicksToday() {
        return clicksToday;
    }

    public long getClicksThisWeek() {
        return clicksThisWeek;
    }

    public long getClicksThisMonth() {
        return clicksThisMonth;
    }

    public Map<String, Long> getBrowsers() {
        return browsers;
    }

    public Map<String, Long> getOperatingSystems() {
        return operatingSystems;
    }

    public Map<String, Long> getDevices() {
        return devices;
    }
}