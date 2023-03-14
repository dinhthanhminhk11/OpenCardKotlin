package com.example.opencardkotlin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opencardkotlin.models.BoardSize
import com.example.opencardkotlin.utils.BitmapScaler
import com.example.opencardkotlin.utils.EXTRA_BOARD_SIZE
import com.example.opencardkotlin.utils.isPermissionGrated
import com.example.opencardkotlin.utils.requestPermission
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
    companion object {
        private const val PICK_PHOTO_CODE = 655
        private const val RED_EXTERNAL_PHOTOS = 248
        private const val READ_PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_LENGTH = 3
        private const val MAX_GAME_LENGTH = 14
    }

    private lateinit var adapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize
    private var numberImageRequired = -1
    private val btnSave: Button by lazy { findViewById<Button>(R.id.btnSave) }
    private val editName: EditText by lazy { findViewById<EditText>(R.id.editName) }
    private val rvImagePicker: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rvImagePicker) }
    private val chosenImageUris = mutableListOf<Uri>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numberImageRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics ( 0 / ${numberImageRequired} )"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ImagePickerAdapter(
            this,
            chosenImageUris,
            boardSize,
            object : ImagePickerAdapter.ImageClickListener {
                override fun onPlaceholderClick() {
                    if (isPermissionGrated(this@CreateActivity, READ_PHOTO_PERMISSION)) {
                        launchIntentForPhotos()
                    } else {
                        requestPermission(
                            this@CreateActivity, READ_PHOTO_PERMISSION, RED_EXTERNAL_PHOTOS
                        )
                    }
                }
            })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWith())

        btnSave.setOnClickListener {
            saveDataToFirebase()
        }

        editName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_LENGTH))// max leng
        editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == RED_EXTERNAL_PHOTOS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentForPhotos()
            } else {
                Toast.makeText(this, "looix", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.w("MINH", "not null")
            return
        }

        val selectedUri = data.data
        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if (chosenImageUris.size < numberImageRequired) {
                    chosenImageUris.add(clipItem.uri)
                }
            }
        } else if (selectedUri != null) {
            chosenImageUris.add(selectedUri)
        }

        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics ( ${chosenImageUris.size} / $numberImageRequired )"
        btnSave.isEnabled = shouldEnableSaveButton()
    }

    private fun shouldEnableSaveButton(): Boolean {
        if (chosenImageUris.size != numberImageRequired) {
            return false
        }
        if (editName.text.isBlank() || editName.text.length < MIN_GAME_LENGTH) {
            return false
        }
        return true
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTO_CODE)
    }

    private fun saveDataToFirebase() {
        for ((index, photoUri) in chosenImageUris.withIndex()) {
            val imageByteArray = getImageByteArray(photoUri)
        }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        val scaleBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaleBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }
}