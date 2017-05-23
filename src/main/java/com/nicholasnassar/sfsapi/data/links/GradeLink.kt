package com.nicholasnassar.sfsapi.data.links

import io.vertx.core.json.JsonObject

class GradeLink(val classId: String, val cgpId: String) : Link(LinkType.GRADE) {
    override fun asJson(): JsonObject {
        return JsonObject().put("type", type.name).put("class_id", classId).put("cgp_id", cgpId)
    }
}
