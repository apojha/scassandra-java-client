package org.scassandra.http.client;

import java.util.List;
import java.util.Map;

public class PrimingRequest {
    private String when;
    private Then then;

    public PrimingRequest(String when, List<Map<String, String>> rows) {
        this.when = when;
        this.then = new Then(rows);
    }

    @Override
    public String toString() {
        return "PrimingRequest{" +
                "when='" + when + '\'' +
                ", then=" + then +
                '}';
    }

    private static class Then {
        private List<Map<String, String>> rows;
        private Result result;

        private Then(List<Map<String, String>> rows) {
            this.rows = rows;
            this.result = Result.success;
        }
    }

    public static enum Result {
        success,
        read_request_timeout,
        unavailable,
        write_request_timeout
    }
}
