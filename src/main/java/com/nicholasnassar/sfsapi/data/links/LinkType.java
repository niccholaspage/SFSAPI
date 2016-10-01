package com.nicholasnassar.sfsapi.data.links;

public enum LinkType {
    GRADE("grade", "parents/StudentProgressView.aspx", "ClassID="),
    ASSIGNMENT("assignment", "AssignmentView.aspx", "TestNameID="),
    ANNOUNCEMENT("announcement", "parents/AnnouncementView.aspx", "AnnouncementID=");

    private final String name;

    private final String[] matching;

    LinkType(String name, String... matching) {
        this.name = name;

        this.matching = matching;
    }

    public String getName() {
        return name;
    }

    private static LinkType matchType(String match) {
        outerLoop:
        for (LinkType type : values()) {
            for (String matchingPhrase : type.matching) {
                if (!match.contains(matchingPhrase)) {
                    continue outerLoop;
                }
            }

            return type;
        }

        return null;
    }

    public static Link generateLink(String url) {
        LinkType type = matchType(url);

        if (type == null) {
            return null;
        }

        if (type == GRADE) {
            url = url.substring(url.indexOf("ClassID=") + 8);

            url = url.substring(0, url.indexOf("&"));
        } else if (type == ASSIGNMENT) {
            url = url.substring(url.indexOf("TestNameID=") + 11);

            int index = url.indexOf("&");

            if (index != -1) {
                url = url.substring(0, index);
            }
        } else if (type == ANNOUNCEMENT) {
            url = url.substring(url.indexOf("AnnouncementID=") + 15);
        }

        return new Link(type, url);
    }
}
