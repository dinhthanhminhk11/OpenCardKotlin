package com.example.opencardkotlin.models

import com.example.opencardkotlin.utils.DEFAULT_ICON

class MemoryGame(private val boardSize: BoardSize) {
    val cards: List<MemoryCard>
    var numPairsFound = 0
    private var indexOfSingleSelectedCard: Int? = null

    private var numCardFlips = 0

    init {
        val chosenImages = DEFAULT_ICON.shuffled().take(boardSize.getNumPairs())
        val ramdomizedImages = (chosenImages + chosenImages).shuffled() //  sắp xếp lại
        cards = ramdomizedImages.map {
            MemoryCard(it)
        }// gán dự liệu sang 1 list mới ví dụ từ đầu nó list int h nó đã thành list đối tượng memoryCard
    }

    fun flipCard(position: Int): Boolean {
        numCardFlips++
        var foundMatch = false;
        val card = cards[position]
        if (indexOfSingleSelectedCard == null) {
            restoreCard()
            indexOfSingleSelectedCard = position;
        } else {
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null;
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier) {
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCard() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numCardFlips / 2
    }
}