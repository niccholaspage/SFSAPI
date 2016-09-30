package com.nicholasnassar.sfsapi.data.grades;

import com.nicholasnassar.sfsapi.data.links.Link;
import com.nicholasnassar.sfsapi.data.links.LinkType;
import io.vertx.core.json.JsonObject;

public class Grade {
    private final Link link;

    private final String term, clazz, instructor, score;

    public Grade(String id, String term, String clazz, String instructor, String score) {
        this.link = new Link(LinkType.GRADE, id);

        this.term = term;

        this.clazz = clazz;

        this.instructor = instructor;

        this.score = score;
    }

    public String getTerm() {
        return term;
    }

    public String getClazz() {
        return clazz;
    }

    public String getScore() {
        return score;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link.asJson()).put("term", term).put("class", clazz)
                .put("instructor", instructor).put("score", score);
    }
}
