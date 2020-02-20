package com.rifafauzi.customcamerasurfaceview.common

import androidx.annotation.StringRes

/**
 * Created by rrifafauzikomara on 2020-02-19.
 */
 
sealed class ResultState<out T> {
    data class NoInternetConnection<out T> (@StringRes val errorMessage : Int) : ResultState<T>()
    class Loading<out T> : ResultState<T>()
    class NoData<out T> : ResultState<T>()
    data class HasData<out T>(val data : T) : ResultState<T>()
    data class Error<out T> (@StringRes val errorMessage : Int): ResultState<T>()
    data class Timeout<out T>(@StringRes val errorMessage : Int): ResultState<T>()
}