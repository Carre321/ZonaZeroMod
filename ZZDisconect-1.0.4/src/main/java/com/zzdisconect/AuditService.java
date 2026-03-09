package com.zzdisconect;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class AuditService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final ZZDisconect plugin;

    public AuditService(ZZDisconect plugin) {
        this.plugin = plugin;
    }

    public void log(String action, String actor, String details) {
        String stamp = TS.format(Instant.now().atOffset(ZoneOffset.UTC));
        plugin.getLogger().at(Level.INFO).log("[AUDIT] ts=%s action=%s actor=%s details=%s", stamp, action, actor, details == null ? "" : details);
    }
}
