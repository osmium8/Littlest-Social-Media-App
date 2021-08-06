package com.example.snplc.other

/**
 * used for :
 * verifying network calls
 * determining state of data : T?
 *
 * @SealedClass
 * impose restriction on inheritance, only classes in here can inherit
 */


sealed class Resource<T> (val data: T? = null, val message: String? = null){
    // data -> user
    // message -> errors

    class Success<T> (data: T) : Resource<T>(data)
    class Error<T> (message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> (data: T? = null) : Resource<T>(data)
}