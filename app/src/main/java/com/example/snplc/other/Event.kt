package com.example.snplc.other

import androidx.lifecycle.Observer

/**
 * consumes events | they fire-off just once
 *
 * (snackbar don't appear twice when we rotate the device)
 */
class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // only changed within this class, readable from other classes

    fun getContentIfNotHandled(): T? {
        return if(!hasBeenHandled) {
            hasBeenHandled = true
            content
        } else null
    }

    fun peekContent() = content


}

/**
 * It helps us to only emit errors once, but success and loading status multiple times
 * also saves boilerplate code, we call observer many times
 */
class EventObserver<T>(
    private inline val onError: ((String) -> Unit)? = null, // takes error message
    private inline val onLoading: (() -> Unit)? = null,
    private inline val onSuccess: (T) -> Unit // not nullable
) : Observer<Event<Resource<T>>> {


    override fun onChanged(t: Event<Resource<T>>?) {

        /**
         * consume Event only for error
         * for this
         * t.getContentIfNotHandled() should only be used for error
         */
        when(val content = t?.peekContent()) {

            is Resource.Success -> {
                content.data?.let(onSuccess) // called lambda received
            }

            is Resource.Error -> {
                t.getContentIfNotHandled()?.let {
                    /**
                     * content not consumed
                     */
                    onError?.let { error ->
                        error(it.message!!)
                    }
                }
            }

            is Resource.Loading -> {
                onLoading?.let { loading ->
                    loading()
                }
            }
        }
    }
}