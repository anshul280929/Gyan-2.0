package com.gyan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public void log(String action, String subject, String outcome, String details) {
        auditLogger.info("action={} subject={} outcome={} details={}", action, safe(subject), safe(outcome), safe(details));
    }

    public void logAuth(String action, String subject, String outcome) {
        log(action, subject, outcome, "");
    }

    private String safe(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n]+", " ").trim();
    }
}
