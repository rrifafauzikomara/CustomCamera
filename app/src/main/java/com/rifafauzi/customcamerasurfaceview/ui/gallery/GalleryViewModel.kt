package com.rifafauzi.customcamerasurfaceview.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rifafauzi.customcamerasurfaceview.R
import com.rifafauzi.customcamerasurfaceview.api.ApiClient
import com.rifafauzi.customcamerasurfaceview.model.KTPModel
import com.rifafauzi.customcamerasurfaceview.common.ResultState
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * Created by rrifafauzikomara on 2020-02-19.
 */
 
class GalleryViewModel : ViewModel() {

    private val _nik = MutableLiveData<ResultState<List<KTPModel>>>()
    val nik: LiveData<ResultState<List<KTPModel>>> get() = _nik

    private fun setResultCamera(resultState: ResultState<List<KTPModel>>) {
        _nik.postValue(resultState)
    }

    fun getListKTP(nik: Long) {
        setResultCamera(ResultState.Loading())
        viewModelScope.launch {
            val response = ApiClient.retrofitService.searchNIK(nik)
            val result = response.content
            try {
                if (result.isEmpty() || result[0].RESPON.equals("Data Tidak Ditemukan")) {
                    setResultCamera(ResultState.NoData())
                    return@launch
                }
                setResultCamera(ResultState.HasData(result))
            } catch (e: Throwable) {
                when (e) {
                    is IOException -> setResultCamera(ResultState.NoInternetConnection(R.string.no_internet_connection))
                    is TimeoutException ->  setResultCamera(ResultState.Timeout(R.string.timeout))
                    else -> setResultCamera(ResultState.Error(R.string.unknown_error))
                }
            }
        }
    }


}