package com.nicholasnassar.sfsapi.data.links;

public enum LetterGrade {
    F("F"),
    D_MINUS("D-"),
    D("D"),
    D_PLUS("D+"),
    C_MINUS("C-"),
    C("C"),
    C_PLUS("C+"),
    B_MINUS("B-"),
    B("B"),
    B_PLUS("B+"),
    A_MINUS("A-"),
    A("A"),
    A_PLUS("A+");

    private final String name;

    LetterGrade(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static LetterGrade getLetterGrade(String name) {
        for (LetterGrade grade : values()) {
            if (grade.getName().equals(name)) {
                return grade;
            }
        }

        return null;
    }
}
