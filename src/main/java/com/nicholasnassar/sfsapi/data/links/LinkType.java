package com.nicholasnassar.sfsapi.data.links;

public enum LinkType {
    GRADE("grade", "StudentProgressView.aspx|StudentProgressDetails.aspx", "ClassID="),
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
                String[] matchingPhrases = matchingPhrase.split("\\|");

                boolean hitOne = false;

                for (String goodName : matchingPhrases) {
                    if (match.contains(goodName)) {
                        hitOne = true;
                    }
                }

                if (!hitOne) {
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
            String classId = url.substring(url.indexOf("ClassID=") + 8);

            classId = classId.substring(0, classId.indexOf("&"));

            String cgpId = url.substring(url.indexOf("CGPID=") + 6);

            if (cgpId.contains("&")) {
                cgpId = cgpId.substring(0, cgpId.indexOf("&"));
            }

            return new GradeLink(classId, cgpId);
        } else if (type == ASSIGNMENT) {
            url = url.substring(url.indexOf("TestNameID=") + 11);

            int index = url.indexOf("&");

            if (index != -1) {
                url = url.substring(0, index);
            }

            return new AssignmentLink(url);
        } else if (type == ANNOUNCEMENT) {
            url = url.substring(url.indexOf("AnnouncementID=") + 15);

            return new AnnouncementLink(url);
        }

        return null;
    }
}
