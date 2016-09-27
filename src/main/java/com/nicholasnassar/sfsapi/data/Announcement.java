package com.nicholasnassar.sfsapi.data;

import io.vertx.core.json.JsonObject;

public class Announcement {
    private final String from, to, date, subject, content;

    public Announcement(String from, String to, String date, String subject, String content) {
        this.from = from;

        this.to = to;

        this.date = date;

        this.subject = subject;

        this.content = content;
    }

    public JsonObject asJson() {
        return new JsonObject().put("from", from).put("to", to).put("date", date).put("subject", subject)
                .put("content", content);
    }
}
