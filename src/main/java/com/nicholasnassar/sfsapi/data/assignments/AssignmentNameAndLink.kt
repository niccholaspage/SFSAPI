package com.nicholasnassar.sfsapi.data.assignments

import com.nicholasnassar.sfsapi.data.links.Link
import io.vertx.core.json.JsonObject

class AssignmentNameAndLink(val name: String, val link: Link) {
    fun asJson(): JsonObject {
        return JsonObject().put("name", name).put("link", link.asJson())
    }
}
