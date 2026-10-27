package com.bignerdranch.android.pr5_2_api

import com.google.gson.annotations.SerializedName

data class DeezerSearchResponse(
    @SerializedName("data") val data: List<DeezerTrack>?
)

data class DeezerTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: DeezerArtist?,
    @SerializedName("duration") val durationSec: Int,
    @SerializedName("preview") val previewUrl: String?,
    @SerializedName("link") val link: String?
)

data class DeezerArtist(
    @SerializedName("name") val name: String?
)