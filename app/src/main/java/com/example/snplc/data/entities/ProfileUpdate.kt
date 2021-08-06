package com.example.snplc.data.entities

import android.net.Uri

/** parameters we need to update a user's profile **/
data class ProfileUpdate(
    val uidToUpdate: String = "",
    val username: String = "",
    val description: String = "",
    val profilePictureUri: Uri? = null
)
