package com.nicholasnassar.sfsapi.data.grades;

public enum GradeScale {
    COLLEGE_PREP(0, 1, 1.333, 1.667, 2, 2.333, 2.667, 3, 3.333, 3.667, 4, 4.333, 4.667),
    HONORS(0, 1.333, 1.667, 2, 2.333, 2.667, 3, 3.333, 3.667, 4, 4.333, 4.667, 5),
    AP(0, 1.667, 2, 2.333, 2.667, 3, 3.333, 3.667, 4, 4.333, 4.667, 5, 5.333);

    private final double[] scores;

    GradeScale(double... scores) {
        this.scores = scores;
    }

    public double getScore(LetterGrade letterGrade) {
        return scores[letterGrade.ordinal()];
    }

    public static GradeScale getScale(String className) {
        if (className.startsWith("AP ")) {
            return AP;
        } else if (className.startsWith("Honors ")) {
            return HONORS;
        } else {
            return COLLEGE_PREP;
        }
    }
}
