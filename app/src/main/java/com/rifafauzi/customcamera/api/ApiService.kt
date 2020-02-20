package com.rifafauzi.customcamera.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by rrifafauzikomara on 2020-02-19.
 */

interface ApiService {

    @GET("nikdata")
    suspend fun searchNIK(@Query("nik") nik: Long) : KTPResponse

}