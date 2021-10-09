package com.example.snplc.repositories

import android.net.Uri
import android.util.Log
import com.example.snplc.data.entities.Comment
import com.example.snplc.data.entities.Post
import com.example.snplc.data.entities.ProfileUpdate
import com.example.snplc.data.entities.User
import com.example.snplc.other.Constants.DEFAULT_PROFILE_PICTURE_URL
import com.example.snplc.other.Resource
import com.example.snplc.other.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

/**
 * @ActivityScoped
 * We need only one instance
 * throughout the lifetime of our MainActivity
 *
 * it will be injected to multiple viewModels of MainActivity's fragments
 */

@ActivityScoped
class DefaultMainRepository : MainRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage

    private val users = firestore.collection("users")
    private val posts = firestore.collection("posts")
    private val comments = firestore.collection("comments")

    override suspend fun createPost(imageUri: Uri, text: String) = withContext(Dispatchers.IO) {
        safeCall {
            val uid = auth.uid!! // can be asserted because we will be in MainActivity if user logged in

            // generates a unique long string
            val postId = UUID.randomUUID().toString()

            // upload image from URI(local storage address) to firebase storage
            val imageUploadResult = storage.getReference(postId).putFile(imageUri).await()

            // get URL after uploading to firebase
            val imageUrl = imageUploadResult?.metadata?.reference?.downloadUrl?.await().toString()

            //set new Post document
            val post = Post(
                id = postId,
                authorUid = uid,
                text = text,
                imageUrl = imageUrl,
                date = System.currentTimeMillis()
            )
            posts.document(postId).set(post).await() // returns TASK so use await()
            Resource.Success(Any())
        }
    }

    // get all user documents from users-collection in this list
    override suspend fun getUsers(uids: List<String>) = withContext(Dispatchers.IO) {
        safeCall {
            val usersList = users.whereIn("uid", uids)
                                .orderBy("username")
                                .get()
                                .await()
                                .toObjects(User::class.java)
            Resource.Success(usersList)
        }
    }

    /**
     * get a single user to show his/her profile
     */
    override suspend fun getUser(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val user = users.document(uid).get().await().toObject(User::class.java)
                ?: throw IllegalStateException()
            val currentUid = FirebaseAuth.getInstance().uid!!
            val currentUser = users.document(currentUid).get().await().toObject(User::class.java)
                ?: throw IllegalStateException()

            // if uid in list of follows, turn ON isFollowing
            user.isFollowing = uid in currentUser.follows
            Resource.Success(user)
        }
    }

    override suspend fun getPostsForFollows() = withContext(Dispatchers.IO) {
        safeCall {
            val uid = FirebaseAuth.getInstance().uid!! // logged in person
            val follows = getUser(uid).data!!.follows

            /**
             * get all posts that are in @follows list
             *
             * @onEach
             * loop over list and manually assign values to @Exclude variables
             */
            val allPosts = posts.whereIn("authorUid", follows)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
                .onEach { post ->
                    val user = getUser(post.authorUid).data!!
                    post.authorProfilePictureUrl = user.profilePictureUrl
                    post.authorUsername = user.username
                    post.isLiked = uid in post.likedBy // if logged-in user is in likedBy list
                }
            Resource.Success(allPosts)
        }
    }

    override suspend fun toggleLikeForPost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            var isLiked = false
            /**
             * @fire store-transaction will be executed only if all the calls succeed
             *
             * read then update
             */
            firestore.runTransaction { transaction ->
                val uid = FirebaseAuth.getInstance().uid!!
                val postResult = transaction.get(posts.document(post.id))
                val currentLikes = postResult.toObject(Post::class.java)?.likedBy ?: listOf()

                /** update likedBy field **/
                transaction.update(
                    posts.document(post.id),
                    "likedBy",
                    if(uid in currentLikes) {
                        currentLikes - uid
                    }
                    else {
                        isLiked = true
                        currentLikes + uid // last line is returned
                    }
                )
            }.await()
            Resource.Success(isLiked)
        }
    }


    /**
     * delete post document + delete image(in firebase-storage) that belong to that post
     */
    override suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        safeCall {
            posts.document(post.id).delete().await()
            storage.getReferenceFromUrl(post.imageUrl).delete().await()
            Resource.Success(post)
        }
    }

    override suspend fun getPostsForProfile(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            val profilePosts = posts.whereEqualTo("authorUid", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
                .onEach { post ->
                    /** same as getPostsForFollow() **/
                    val user = getUser(post.authorUid).data!!
                    post.authorProfilePictureUrl = user.profilePictureUrl
                    post.authorUsername = user.username
                    post.isLiked = uid in post.likedBy
                }
            Resource.Success(profilePosts)
        }
    }

    override suspend fun toggleFollowForUser(uid: String) = withContext(Dispatchers.IO) {
        safeCall {
            var isFollowing = false
            firestore.runTransaction { transaction ->
                val currentUid = auth.uid!!
                val currentUser = transaction.get(users.document(currentUid)).toObject(User::class.java)!!
                isFollowing = uid in currentUser.follows
                val newFollows = if(isFollowing) currentUser.follows - uid else currentUser.follows + uid
                transaction.update(users.document(currentUid), "follows", newFollows)
            }.await()
            Resource.Success(!isFollowing)
        }
    }

    override suspend fun searchUser(query: String) = withContext(Dispatchers.IO) {
        safeCall {
            /** @whereGreaterThanOrEqualTo
             * toUpperCase -> to get all users with lowercase user name
             * uppercase < lowercase
             * **/
            val userResults =
                users
                .whereGreaterThanOrEqualTo("username", query.toUpperCase(Locale.ROOT))
                .get().await().toObjects(User::class.java)

            Resource.Success(userResults)
        }
    }

    override suspend fun createComment(commentText: String, postId: String) =
        withContext(Dispatchers.IO) {
            safeCall {
                val uid = auth.uid!!
                val commentId = UUID.randomUUID().toString()
                val user = getUser(uid).data!!
                val comment = Comment(
                    commentId,
                    postId,
                    uid,
                    user.username,
                    user.profilePictureUrl,
                    commentText
                )
                comments.document(commentId).set(comment).await()
                // Log.i("Comment", "created Comment()...")
                Resource.Success(comment)
            }
        }

    override suspend fun deleteComment(comment: Comment) = withContext(Dispatchers.IO) {
        safeCall {
            comments.document(comment.commentId).delete().await()
            Resource.Success(comment)
        }
    }

    override suspend fun getCommentForPost(postId: String) = withContext(Dispatchers.IO) {
        safeCall {
            /** postId == postId we pass **/
            val commentsForPost = comments
                .whereEqualTo("postId", postId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Comment::class.java)
                .onEach { comment ->
                    val user = getUser(comment.uid).data!!
                    /** excluded variables from fire store **/
                    comment.username = user.username
                    comment.profilePictureUrl = user.profilePictureUrl
                }
            Resource.Success(commentsForPost)
        }
    }

    override suspend fun updateProfilePicture(uid: String, imageUri: Uri) =
        withContext(Dispatchers.IO) {
            /** update / upload new one **/
            val storageRef = storage.getReference(uid)
            /** file_name = user's uid**/
            val user = getUser(uid).data!!
            if (user.profilePictureUrl != DEFAULT_PROFILE_PICTURE_URL) {
                storage.getReferenceFromUrl(user.profilePictureUrl).delete().await()
            }
            /** get url of just uploaded image **/
            storageRef.putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
        }

    override suspend fun updateProfile(profileUpdate: ProfileUpdate) = withContext(Dispatchers.IO) {
        safeCall {

            /** update image if user selected uri **/
            val imageUrl = profileUpdate.profilePictureUri?.let { uri ->
                updateProfilePicture(profileUpdate.uidToUpdate, uri).toString()
            }

            /** create map to update document in fire store **/
            val map = mutableMapOf(
                "username" to profileUpdate.username,
                "description" to profileUpdate.description
            )

            imageUrl?.let { url ->
                map["profilePictureUrl"] = url
            }

            // convert to immutable map
            users.document(profileUpdate.uidToUpdate).update(map.toMap()).await()

            Resource.Success(Any())
        }
    }
}