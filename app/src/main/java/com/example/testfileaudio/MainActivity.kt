package com.example.testfileaudio

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testfileaudio.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var audioAdapter: AudioAdapter? = null
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
            if (readGranted && writeGranted) {
                setupRecyclerView() // Nếu cả hai quyền được cấp, thiết lập RecyclerView
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        binding.showAudio.setOnClickListener {
            permissionResultLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }

    }


    fun setupRecyclerView() {
        binding.rvcAudio.layoutManager = LinearLayoutManager(this)
        val audioList = queryAudio()
        audioAdapter = AudioAdapter(audioList)
        binding.rvcAudio.adapter = audioAdapter

    }

    private fun queryAudio(): List<AudioModel> {
        val audioList = mutableListOf<AudioModel>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )

        val selection = null
        val selectionArgs = null
        val sortOrder = null

        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val nameColIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateAddedColIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColIndex)
                val name = cursor.getString(nameColIndex)
                val dateAdded = cursor.getLong(dateAddedColIndex)
                val size = cursor.getLong(sizeColIndex)

                val dateAddedFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(dateAdded * 1000))


                val sizeMB = size / (1024f * 1024f)
                val sizeFormatted = String.format("%.2f MB", sizeMB)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                audioList.add(AudioModel(name, dateAddedFormatted, sizeFormatted,contentUri))
            }
        }

        return audioList
    }

}