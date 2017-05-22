package com.nicholasnassar.sfsapi.data.grades

enum class LetterGrade(val myName: String) {
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

    companion object {
        @JvmStatic fun getLetterGrade(name: String): LetterGrade? {
            return values().firstOrNull { it.myName == name }
        }
    }
}