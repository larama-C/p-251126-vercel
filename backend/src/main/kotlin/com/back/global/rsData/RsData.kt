package com.back.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore

class RsData<T> (
    val resultCode: String,
    val msg: String,
    val data: T? = null
) {

    @get:JsonIgnore
    val statusCode: Int
        get() = resultCode.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()

}
