package com.nicholasnassar.sfsapi.data;

import com.nicholasnassar.sfsapi.data.links.Link;
import io.vertx.core.json.JsonObject;

public class NewsFeedItem {
    private final String clazz, time, description;

    private final Link link;

    public NewsFeedItem(String clazz, String time, String description, Link link) {
        this.clazz = clazz;

        this.time = time;

        this.description = description;

        this.link = link;
    }

    public JsonObject asJson() {
        return new JsonObject().put("class", clazz).put("time", time).put("description", description)
                .put("link", link == null ? null : link.asJson());
    }
}
