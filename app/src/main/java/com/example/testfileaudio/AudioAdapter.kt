package com.example.testfileaudio

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class AudioAdapter(private val audioList: List<AudioModel>) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audio = audioList[position]
        holder.bind(audio, position)
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    inner class AudioViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtname)
        private val txtDate: TextView = itemView.findViewById(R.id.date_time)
        private val txtSize: TextView = itemView.findViewById(R.id.size)

        init {
            // Attach long click listener to the item view
            itemView.setOnLongClickListener {
                showPopup(itemView)
                true
            }
        }

        fun bind(audio: AudioModel, position: Int) {
            txtName.text = audio.name
            txtDate.text = audio.date
            txtSize.text = audio.size
        }

        private fun showPopup(view: View) {
            val popupView =
                LayoutInflater.from(view.context).inflate(R.layout.popup_rename_file, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            val newNameEditText: EditText = popupView.findViewById(R.id.newNameEditText)
            val saveButton: Button = popupView.findViewById(R.id.saveButton)
            val deleteButton: Button = popupView.findViewById(R.id.deleteButton)
            deleteButton.setOnClickListener {
                val audio = audioList[position]
                val audioUri = audio.fileName
                deleteAudio(view.context, audioUri)
                deleteAudio(position)
            }
            saveButton.setOnClickListener {
                val newName = newNameEditText.text.toString()
                if (newName.isNotEmpty()) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val audio = audioList[position]
                        val audioUri = audio.fileName // Assuming you have uri field in AudioModel
                        updateAudioName(view.context, audioUri, newName)
                        updateRecyclerView(position, newName)
                    }
                    popupWindow.dismiss()
                } else {
                    Toast.makeText(view.context, "Please enter a new name", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            popupWindow.showAsDropDown(view)
        }
    }

    fun updateAudioName(context: Context, audioUri: Uri, newName: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, newName)
        }
        context.contentResolver.update(audioUri, values, null, null)
    }

    fun deleteAudio(context: Context, audioUri: Uri) {
        try {
            val filePath = getFilePathFromUri(context, audioUri)
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()

                    //scanfile MediaStore
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(file.absolutePath),
                        null,
                        object : MediaScannerConnection.OnScanCompletedListener {
                            override fun onScanCompleted(path: String?, uri: Uri?) {
                                Toast.makeText(context, "cập nhật thành công ", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        })

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        var filePath: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        cursor?.close()
        return filePath
    }

    fun updateRecyclerView(position: Int, newName: String) {
        audioList[position].name = newName
        notifyItemChanged(position)
    }

    fun deleteAudio(position: Int) {
        notifyItemRemoved(position)
    }

    fun deleteFileAndUpdateMediaStore(context: Context, fileUri: Uri) {
        try {
            val fileToDelete = File(fileUri.path)
            if (fileToDelete.exists()) {
                fileToDelete.delete()

                // Gửi tín hiệu để cập nhật MediaStore

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}