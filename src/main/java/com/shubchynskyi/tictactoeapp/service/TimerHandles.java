package com.shubchynskyi.tictactoeapp.service;

import java.util.concurrent.ScheduledFuture;

record TimerHandles(ScheduledFuture<?> warningFuture, ScheduledFuture<?> closeFuture) {
    public void cancelAll(boolean mayInterruptIfRunning) {
        if (warningFuture != null) {
            warningFuture.cancel(mayInterruptIfRunning);
        }
        if (closeFuture != null) {
            closeFuture.cancel(mayInterruptIfRunning);
        }
    }
}