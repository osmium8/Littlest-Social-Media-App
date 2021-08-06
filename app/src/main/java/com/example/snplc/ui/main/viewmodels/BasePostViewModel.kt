package com.example.snplc.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snplc.data.entities.Post
import com.example.snplc.data.entities.User
import com.example.snplc.other.Event
import com.example.snplc.other.Resource
import com.example.snplc.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * abstract class :
 * defines behaviour of every fragment that needs to display a list of posts
 * ,
 * HomeFragment & ProfileFragment's viewModel inherits
 * they both need to display posts
 */
abstract class BasePostViewModel(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    /**
     * toggleLikeForPost() return Resource<Boolean>
     */
    private val _likePostStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val likePostStatus: LiveData<Event<Resource<Boolean>>> = _likePostStatus

    private val _deletePostStatus = MutableLiveData<Event<Resource<Post>>>()
    val deletePostStatus: LiveData<Event<Resource<Post>>> = _deletePostStatus

    private val _likedByUsers = MutableLiveData<Event<Resource<List<User>>>>()
    val likedByUsers: LiveData<Event<Resource<List<User>>>> = _likedByUsers

    /**
     * viewModels' that inherit will have their own implementation of posts & getPosts
     */
    abstract val posts: LiveData<Event<Resource<List<Post>>>>
    abstract fun getPosts(uid: String = "")

    fun getUsers(uids: List<String>) {
        if(uids.isEmpty()) return
        _likedByUsers.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getUsers(uids)
            _likedByUsers.postValue(Event(result))
        }
    }

    fun toggleLikeForPost(post: Post) {
        _likePostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.toggleLikeForPost(post)
            _likePostStatus.postValue(Event(result))
        }
    }

    fun deletePost(post: Post) {
        _deletePostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.deletePost(post)
            _deletePostStatus.postValue(Event(result))
        }
    }
}