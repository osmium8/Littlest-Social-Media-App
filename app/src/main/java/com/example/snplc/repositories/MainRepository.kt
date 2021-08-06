package com.example.snplc.repositories

import android.net.Uri
import com.example.snplc.data.entities.Comment
import com.example.snplc.data.entities.Post
import com.example.snplc.data.entities.ProfileUpdate
import com.example.snplc.data.entities.User
import com.example.snplc.other.Resource

interface MainRepository {

    /**
     * <Any> data don't needed to be passed in Resource
     */
    suspend fun createPost(imageUri: Uri, text: String): Resource<Any>

    suspend fun getUsers(uids: List<String>): Resource<List<User>>

    suspend fun getUser(uid: String): Resource<User>

    suspend fun  getPostsForFollows(): Resource<List<Post>>

    suspend fun toggleLikeForPost(post: Post): Resource<Boolean>

    suspend fun deletePost(post: Post): Resource<Post>

    suspend fun getPostsForProfile(uid: String): Resource<List<Post>>

    suspend fun toggleFollowForUser(uid: String): Resource<Boolean>

    suspend fun searchUser(query: String): Resource<List<User>>

    suspend fun createComment(commentText: String, postId: String): Resource<Comment>

    suspend fun deleteComment(comment: Comment): Resource<Comment>

    suspend fun getCommentForPost(postId: String): Resource<List<Comment>>

    /** Any | we don't care about result here **/
    suspend fun updateProfile(profileUpdate: ProfileUpdate): Resource<Any>

    suspend fun updateProfilePicture(uid: String, imageUri: Uri): Uri?
}