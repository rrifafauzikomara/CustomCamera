package com.rifafauzi.customcamera.api

import com.google.gson.annotations.SerializedName
import com.rifafauzi.customcamera.model.KTPModel

/**
 * Created by rrifafauzikomara on 2020-02-19.
 */
 
class KTPResponse(
    @SerializedName("number")
    val number: Int?,
    @SerializedName("lastPage")
    val lastPage: Boolean,
    @SerializedName("numberOfElements")
    val numberOfElements: Int?,
    @SerializedName("firstPage")
    val firstPage: Boolean,
    @SerializedName("size")
    val size: Int?,
    @SerializedName("sort")
    val sort: String?,
    @SerializedName("content")
    val content: List<KTPModel>,
    @SerializedName("totalElements")
    val totalElements: Int?
)