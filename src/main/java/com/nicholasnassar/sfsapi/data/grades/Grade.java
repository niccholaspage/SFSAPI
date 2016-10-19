package com.nicholasnassar.sfsapi.data.grades;

import com.nicholasnassar.sfsapi.data.links.GradeLink;
import io.vertx.core.json.JsonObject;

public class Grade {
    private final GradeLink link;

    private final String term, clazz, instructor, score;

    public Grade(GradeLink link, String term, String clazz, String instructor, String score) {
        this.link = link;

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
