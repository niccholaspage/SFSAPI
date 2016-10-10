package com.nicholasnassar.sfsapi.data.links;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class Resource {
    private final String name, link;

    private final int type;

    public static final int RESOURCE_INTERNAL = 0;

    public static final int RESOURCE_EXTERNAL = 1;

    public Resource(String name, String link) {
        this.name = name;

        this.link = link;

        if (link.toLowerCase().startsWith("/common/")) {
            type = RESOURCE_INTERNAL;
        } else {
            type = RESOURCE_EXTERNAL;
        }
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public int getType() {
        return type;
    }

    public JsonObject asJson() {
        return new JsonObject().put("name", name).put("link", link).put("type", type);
    }

    public static JsonArray asJsonArray(List<Resource> resources) {
        JsonArray array = new JsonArray();

        resources.forEach(resource -> array.add(resource.asJson()));

        return array;
    }
}
