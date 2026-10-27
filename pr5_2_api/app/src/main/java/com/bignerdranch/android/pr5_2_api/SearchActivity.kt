package com.bignerdranch.android.pr5_2_api

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.net.URLEncoder

class SearchActivity : AppCompatActivity() {

    private lateinit var llSearchContainer: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val etSearchQuery = findViewById<EditText>(R.id.etSearchQuery)
        val btnFetchApi = findViewById<Button>(R.id.btnFetchApi)
        llSearchContainer = findViewById(R.id.llSearchContainer)
        val btnGoToPlaylist = findViewById<Button>(R.id.btnGoToPlaylist)

        db = AppDatabase.getDatabase(this)

        btnFetchApi.setOnClickListener {
            val query = etSearchQuery.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "Введите поисковый запрос!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performSearch(query)
        }

        btnGoToPlaylist.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }
    }

    private fun performSearch(query: String) {
        llSearchContainer.removeAllViews()
        Toast.makeText(this, "Поиск...", Toast.LENGTH_SHORT).show()

        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://api.deezer.com/search?q=$encoded"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val parsed = Gson().fromJson(response, DeezerSearchResponse::class.java)
                    val items = parsed.data ?: emptyList()
                    if (items.isEmpty()) {
                        Toast.makeText(this, "Ничего не найдено", Toast.LENGTH_SHORT).show()
                        return@StringRequest
                    }
                    for (track in items) addTrackRow(track)
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка парсинга: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Ошибка запроса: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun addTrackRow(track: DeezerTrack) {
        val rowLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(24, 24, 24, 24)
        }

        val textLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val artistName = track.artist?.name ?: "Unknown"
        val durationStr = formatDuration(track.durationSec)

        textLayout.addView(TextView(this).apply {
            text = track.title
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        })
        textLayout.addView(TextView(this).apply {
            text = "$artistName • $durationStr"
            setTextColor(Color.parseColor("#A7A7A7"))
            textSize = 14f
            setPadding(0, 4, 0, 0)
        })
        rowLayout.addView(textLayout)

        rowLayout.addView(Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Добавить"
            textSize = 12f
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1DB954"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val entity = TrackEntity(
                    id = track.id.toString(),
                    trackName = track.title,
                    artistName = artistName,
                    duration = durationStr,
                    spotifyUrl = track.link ?: ""
                )

                Thread {
                    db.trackDao().insertTrack(entity)
                    // Возвращаемся в главный поток для показа Toast
                    runOnUiThread {
                        Toast.makeText(this@SearchActivity, "Добавлено в плейлист!", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
        })

        llSearchContainer.addView(rowLayout)
    }

    private fun formatDuration(totalSec: Int): String {
        return "%d:%02d".format(totalSec / 60, totalSec % 60)
    }
}