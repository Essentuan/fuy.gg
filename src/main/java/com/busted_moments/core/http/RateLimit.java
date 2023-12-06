package com.busted_moments.core.http;

import com.busted_moments.core.heartbeat.Scheduler;
import com.busted_moments.core.heartbeat.annotations.Schedule;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.util.Priority;

import java.net.http.HttpResponse;

public enum RateLimit implements Scheduler {
    WYNNCRAFT() {
        protected static final String LIMIT_HEADER = "Ratelimit-Limit";
        protected static final String REMAINING_HEADER = "Ratelimit-Remaining";
        protected static final String RESET_HEADER = "Ratelimit-Reset";

        protected int MAX_REQUESTS = 180;

        protected int REMAINING_REQUESTS = 180;

        protected int RATELIMIT_RESET = 60;

        @Schedule(rate = 1, unit = ChronoUnit.SECONDS)
        private void RATELIMIT_RESET() {
            if (RATELIMIT_RESET > 0) {
                RATELIMIT_RESET--;
            } else {
                RATELIMIT_RESET = 60;

                REMAINING_REQUESTS = MAX_REQUESTS;
            }
        }

        @Override
        public boolean canRequest(AbstractRequest<?> request) {
            if (request.getPriority().equals(Priority.CRITICAL)) {
                return REMAINING_REQUESTS > 0;
            } else {
                return REMAINING_REQUESTS - 25 > 0;
            }
        }

        @Override
        public void handleResponse(HttpResponse<String> response) {
            response.headers().firstValue(LIMIT_HEADER).ifPresent(string -> MAX_REQUESTS = Integer.parseInt(string));
            response.headers().firstValue(REMAINING_HEADER).ifPresent(string -> REMAINING_REQUESTS = Integer.parseInt(string));
            response.headers().firstValue(RESET_HEADER).ifPresent(string -> RATELIMIT_RESET = Integer.parseInt(string));
        }

        @Override
        public void handleRequest(AbstractRequest<?> request) {
            REMAINING_REQUESTS--;
        }
    },

    NONE() {
        @Override
        public boolean canRequest(AbstractRequest<?> request) {
            return true;
        }

        @Override
        public void handleResponse(HttpResponse<String> response) throws Exception {

        }

        @Override
        public void handleRequest(AbstractRequest<?> request) {}
    };

    RateLimit() {
        init();

        REGISTER_TASKS();
    }

    void init() {}

    public abstract boolean canRequest(AbstractRequest<?> request);

    public abstract void handleResponse(HttpResponse<String> response) throws Exception;

    public abstract void handleRequest(AbstractRequest<?> request);
}
