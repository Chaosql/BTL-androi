package com.example.myapplication.data.remote.dto.request;

public final class AccountStatusRequest {
    public final String reason;
    public final String reportId;

    public AccountStatusRequest(String reason, String reportId) {
        this.reason = reason.trim();
        this.reportId = reportId == null || reportId.trim().isEmpty() ? null : reportId.trim();
    }
}
