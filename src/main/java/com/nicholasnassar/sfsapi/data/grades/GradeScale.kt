package com.nicholasnassar.sfsapi.data.grades

enum class GradeScale(vararg private val scores: Double) {
    COLLEGE_PREP(0.0, 1.0, 1.333, 1.667, 2.0, 2.333, 2.667, 3.0, 3.333, 3.667, 4.0, 4.333, 4.667),
    HONORS(0.0, 1.333, 1.667, 2.0, 2.333, 2.667, 3.0, 3.333, 3.667, 4.0, 4.333, 4.667, 5.0),
    AP(0.0, 1.667, 2.0, 2.333, 2.667, 3.0, 3.333, 3.667, 4.0, 4.333, 4.667, 5.0, 5.333);

    fun getScore(letterGrade: LetterGrade): Double {
        return scores[letterGrade.ordinal]
    }

    companion object {
        @JvmStatic fun getScale(className: String): GradeScale {
            if (className.startsWith("AP ")) {
                return AP
            } else if (className.startsWith("Honors ")) {
                return HONORS
            } else {
                return COLLEGE_PREP
            }
        }
    }
}
