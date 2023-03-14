package com.example.opencardkotlin.models

enum class BoardSize(val numCard: Int) {
    EASY(8), MEDIUM(18), HARD(24);

    fun getWith(): Int {
        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight(): Int {
        return numCard / getWith()
    }

    fun getNumPairs(): Int {
        return numCard / 2
    }
}