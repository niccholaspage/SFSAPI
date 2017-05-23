package com.nicholasnassar.sfsapi.data.links

import io.vertx.core.json.JsonObject

class AssignmentLink(val id: String) : Link(LinkType.ASSIGNMENT) {
    override fun asJson(): JsonObject {
        return JsonObject().put("type", type.dataName).put("id", id)
    }
}
