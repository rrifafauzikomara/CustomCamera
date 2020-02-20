package com.rifafauzi.customcamera.model

import com.google.gson.annotations.SerializedName

/**
 * Created by rrifafauzikomara on 2020-02-19.
 */

data class KTPModel(

    @SerializedName("RESPON")
    val RESPON: String?,

    @SerializedName("TMPT_LHR")
    val TMPT_LHR: String?,

    @SerializedName("NO_KEL")
    val NO_KEL: String?,

    @SerializedName("NAMA_LGKP_IBU")
    val NAMA_LGKP_IBU: String?,

    @SerializedName("KEL_NAME")
    val KEL_NAME: String?,

    @SerializedName("NO_KK")
    val NO_KK: Long?,

    @SerializedName("NO_RT")
    val NO_RT: Int?,

    @SerializedName("NIK")
    val NIK: Long?,

    @SerializedName("NO_KAB")
    val NO_KAB: Int?,

    @SerializedName("KAB_NAME")
    val KAB_NAME: String?,

    @SerializedName("ALAMAT")
    val ALAMAT: String?,

    @SerializedName("NO_RW")
    val NO_RW: Int?,

    @SerializedName("JENIS_KLMIN")
    val JENIS_KLMIN: String?,

    @SerializedName("NO_KEC")
    val NO_KEC: Int?,

    @SerializedName("NO_PROP")
    val NO_PROP: Int?,

    @SerializedName("PROP_NAME")
    val PROP_NAME: String?,

    @SerializedName("NAMA_LGKP")
    val NAMA_LGKP: String?,

    @SerializedName("KEC_NAME")
    val KEC_NAME: String?,

    @SerializedName("JENIS_PKRJN")
    val JENIS_PKRJN: String?,

    @SerializedName("STATUS_KAWIN")
    val STATUS_KAWIN: String?,

    @SerializedName("TGL_LHR")
    val TGL_LHR: String?
)