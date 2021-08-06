package com.example.snplc.repositories

import com.example.snplc.other.Resource
import com.google.firebase.auth.AuthResult

/**
 * Good practice to define repositories as interface
 * So, that you just define functions you need in the real repository class
 * that implements this interface
 * also good for testing
 */
interface AuthRepository {

    /**
     * returns custom Resource<T>, the class we created
     */
    suspend fun register(email: String, username: String, password: String): Resource<AuthResult>

    suspend fun login(email: String, password: String): Resource<AuthResult>
}