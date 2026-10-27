package com.bignerdranch.android.pr5_2_api

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PlaylistActivity : AppCompatActivity() {

    private lateinit var llPlaylistContainer: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        llPlaylistContainer = findViewById(R.id.llPlaylistContainer)
        val btnBack = findViewById<Button>(R.id.btnBackToSearch)
        db = AppDatabase.getDatabase(this)

        btnBack.setOnClickListener { finish() }

        // observe автоматически обновит экран при изменении базы данных
        db.trackDao().getAllTracks().observe(this) { tracks: List<TrackEntity> ->
            renderPlaylist(tracks)
        }
    }

    private fun renderPlaylist(tracks: List<TrackEntity>) {
        llPlaylistContainer.removeAllViews()

        if (tracks.isEmpty()) {
            llPlaylistContainer.addView(TextView(this).apply {
                text = "Плейлист пуст. Найдите трек на экране поиска и нажмите «Добавить»."
                setTextColor(Color.parseColor("#A7A7A7"))
                textSize = 14f
                setPadding(24, 24, 24, 24)
            })
            return
        }

        for (track in tracks) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#1E1E1E"))
                setPadding(24, 24, 24, 24)
            }

            row.addView(TextView(this).apply {
                text = track.trackName
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = "${track.artistName} • ${track.duration}"
                setTextColor(Color.parseColor("#A7A7A7"))
                textSize = 14f
                setPadding(0, 4, 0, 0)
            })

            val btnRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 0)
            }
            btnRow.addView(Button(this).apply {
                text = "Редактировать"
                textSize = 12f
                setOnClickListener { showEditDialog(track) }
            })
            btnRow.addView(Button(this).apply {
                text = "Удалить"
                textSize = 12f
                setOnClickListener {

                    Thread {
                        db.trackDao().deleteTrack(track)
                        runOnUiThread {
                            Toast.makeText(this@PlaylistActivity, "Удалено", Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                }
            })
            row.addView(btnRow)
            llPlaylistContainer.addView(row)
        }
    }

    private fun showEditDialog(track: TrackEntity) {
        val input = EditText(this).apply {
            setText(track.trackName)
            hint = "Название трека"
        }
        AlertDialog.Builder(this)
            .setTitle("Редактировать трек")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                Thread {
                    db.trackDao().updateTrack(track.copy(trackName = newName))
                }.start()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}