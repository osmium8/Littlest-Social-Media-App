package com.example.snplc.repositories

import com.example.snplc.data.entities.User
import com.example.snplc.other.Resource
import com.example.snplc.other.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DefaultAuthRepository: AuthRepository {

    val auth = FirebaseAuth.getInstance() // singleton
    /**
     * Collections of users, every document(represented by dataclass) represents a user
     */
    val users = FirebaseFirestore.getInstance().collection("users") // singleton

    override suspend fun login(email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val  result = auth.signInWithEmailAndPassword(email, password).await()
                Resource.Success(result)
            }
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): Resource<AuthResult> {

        /**
         * switch coroutines context to dispatchers.IO | IO OPERATION @FRIEBASE
         */
        return withContext(Dispatchers.IO) {
            safeCall {
                /**
                 * await call, continue only if result is fetched
                 */
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid!!
                val user = User(uid, username)

                /**
                 * @user
                 * to be added as a document to our firestore database
                 *
                 * await call, because set() return Task
                 */
                users.document(uid).set(user).await()
                Resource.Success(result)
            }
        }
    }
}