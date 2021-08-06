package com.example.snplc.data.entities

import com.example.snplc.other.Constants.DEFAULT_PROFILE_PICTURE_URL
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    /**
     * unique id received from firebase
     *   to be stored in fire store for easy searching
    **/
    val uid: String = "",
    val username: String = "",
    val profilePictureUrl: String = DEFAULT_PROFILE_PICTURE_URL, // points to cloud fire store
    val description: String = "",
    var follows: List<String> = listOf(), // list of uid's a user follows

    @get: Exclude var isFollowing: Boolean = false
    // property if logged in user follows this user,
    // local variable on the device
    // not to be included in firestore


)
