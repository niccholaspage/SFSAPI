package com.nicholasnassar.sfsapi.data.links

import io.vertx.core.json.JsonObject

class AnnouncementLink(val id: String) : Link(LinkType.ANNOUNCEMENT) {
    override fun asJson(): JsonObject {
        return JsonObject().put("type", type.name).put("id", id)
    }
}
