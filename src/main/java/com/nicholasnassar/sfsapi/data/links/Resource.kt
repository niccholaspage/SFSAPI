package com.nicholasnassar.sfsapi.data.links

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class Resource(val name: String, val link: String) {
    val type: Int

    init {
        if (link.toLowerCase().startsWith("/common/")) {
            type = RESOURCE_INTERNAL
        } else {
            type = RESOURCE_EXTERNAL
        }
    }

    fun asJson(): JsonObject {
        return JsonObject().put("name", name).put("link", link).put("type", type)
    }

    companion object {
        val RESOURCE_INTERNAL = 0

        val RESOURCE_EXTERNAL = 1

        fun asJsonArray(resources: List<Resource>): JsonArray {
            val array = JsonArray()

            resources.forEach { resource -> array.add(resource.asJson()) }

            return array
        }
    }
}