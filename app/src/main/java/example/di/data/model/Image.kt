package example.di.data.model

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("id") val name: String,
    @SerializedName("url") val url: String
)
