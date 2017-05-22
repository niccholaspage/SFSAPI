package com.nicholasnassar.sfsapi.data

import com.nicholasnassar.sfsapi.data.links.Link
import io.vertx.core.json.JsonObject

class NewsFeedItem(val clazz: String, val time: String, val description: String, val link: Link?) {
    fun asJson(): JsonObject {
        return JsonObject().put("class", clazz).put("time", time).put("description", description)
                .put("link", link?.asJson())
    }
}