package com.nicholasnassar.sfsapi.data

import io.vertx.core.json.JsonObject

class Announcement(val from: String, val to: String, val date: String, val subject: String, val content: String) {
    fun asJson(): JsonObject {
        return JsonObject().put("from", from).put("to", to).put("date", date).put("subject", subject)
                .put("content", content)
    }
}