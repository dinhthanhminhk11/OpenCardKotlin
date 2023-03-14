package com.example.opencardkotlin

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opencardkotlin.models.BoardSize
import com.example.opencardkotlin.models.MemoryGame
import com.example.opencardkotlin.utils.EXTRA_BOARD_SIZE
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), MemoryBoardApdater.CardClickListener {

    companion object {
        private const val CREATE_REQUEST_CODE = 248
    }

    private val container: ConstraintLayout by lazy { findViewById<ConstraintLayout>(R.id.container) }
    private val rvBoard: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rvBoard) }
    private val tvNumMoves: TextView by lazy { findViewById<TextView>(R.id.tvNumMoves) }
    private val tvNumPairs: TextView by lazy { findViewById<TextView>(R.id.tvNumPairs) }

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardApdater
    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setupBoard()


    }

    private fun setupBoard() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumPairs.text = "Pairs: 0 / 4"
                tvNumMoves.text = "Easy: 4 x 2"
            }
            BoardSize.MEDIUM -> {
                tvNumPairs.text = "Pairs: 0 / 9"
                tvNumMoves.text = "Easy: 6 x 3"
            }
            BoardSize.HARD -> {
                tvNumPairs.text = "Pairs: 0 / 12"
                tvNumMoves.text = "Easy: 6 x 6"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.green))
        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardApdater(this, boardSize, memoryGame.cards, this)

        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWith());
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog("Refrech game", null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
                return true
            }

            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }

            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showAlertDialog(
        title: String, view: View?, positionClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(this).setTitle(title).setView(view).setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                positionClickListener.onClick(null)
            }.show()
    }

    override fun onCardClicked(position: Int) {
        updateGameWithFlip(position)
    }

    @SuppressLint("SetTextI18n")
    private fun updateGameWithFlip(position: Int) {
        if (memoryGame.haveWonGame()) {
            Snackbar.make(container, "Thắng", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.isCardFaceUp(position)) {
            Snackbar.make(container, "Thua", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.flipCard(position)) {

            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.green)
            ) as Int // chia mã màu
            tvNumPairs.setTextColor(color)

            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()) {
                Snackbar.make(container, "wwin", Snackbar.LENGTH_SHORT).show()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
//        memoryGame.flipCard(position)// đổi trạng thái image
        adapter.notifyDataSetChanged()
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroup: RadioGroup by lazy { boardSizeView.findViewById<RadioGroup>(R.id.radioGroup) }

        when (boardSize) {
            BoardSize.EASY -> radioGroup.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroup.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroup.check(R.id.rbHard)
        }

        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            boardSize = when (radioGroup.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroup: RadioGroup by lazy { boardSizeView.findViewById<RadioGroup>(R.id.radioGroup) }

        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener {
            val desiredBoardSize = when (radioGroup.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }
}