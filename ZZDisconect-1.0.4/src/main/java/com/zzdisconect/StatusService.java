package com.zzdisconect;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class StatusService {

    private final AtomicReference<ServerState> state = new AtomicReference<>(ServerState.STARTING);
    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);
    private final AtomicLong shutdownEndEpochMs = new AtomicLong(0L);

    public ServerState getState() {
        return state.get();
    }

    public void setState(ServerState next) {
        state.set(next);
    }

    public boolean isShutdownInProgress() {
        return shutdownInProgress.get();
    }

    public void setShutdownInProgress(boolean value) {
        shutdownInProgress.set(value);
    }

    public void setShutdownEndEpochMs(long epochMs) {
        shutdownEndEpochMs.set(epochMs);
    }

    public void clearShutdownTimer() {
        shutdownEndEpochMs.set(0L);
    }

    public int getShutdownRemainingSeconds() {
        long end = shutdownEndEpochMs.get();
        if (end <= 0L) {
            return 0;
        }
        long now = Instant.now().toEpochMilli();
        long ms = Math.max(0L, end - now);
        return (int) Math.ceil(ms / 1000.0);
    }
}
