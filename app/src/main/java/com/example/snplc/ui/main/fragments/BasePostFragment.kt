package com.example.snplc.ui.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.snplc.R
import com.example.snplc.adapters.PostAdapter
import com.example.snplc.adapters.UserAdapter
import com.example.snplc.other.EventObserver
import com.example.snplc.ui.main.dialogs.DeletePostDialog
import com.example.snplc.ui.main.dialogs.LikedByDialog
import com.example.snplc.ui.main.viewmodels.BasePostViewModel
import com.example.snplc.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

abstract class BasePostFragment(
    layoutId: Int
) : Fragment(layoutId) {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var postAdapter: PostAdapter

    /** ABSTRACT **/
    protected abstract val postProgressBar: ProgressBar // post is loading
    protected abstract val basePostViewModel: BasePostViewModel // access basic post functions

    /** save current index of the liked item **/
    private var curLikedIndex: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        postAdapter.setOnLikeClickListener { post, i ->
            curLikedIndex = i
            post.isLiked = !post.isLiked
            basePostViewModel.toggleLikeForPost(post)
        }

        postAdapter.setOnDeletePostClickListener { post ->
            DeletePostDialog().apply {
                setPositiveListener {
                    basePostViewModel.deletePost(post)
                }
            }.show(childFragmentManager, null)
        }

        postAdapter.setOnLikedByClickListener { post ->
            basePostViewModel.getUsers(post.likedBy)
        }

        postAdapter.setOnCommentsClickListener { comment ->
            findNavController().navigate(
                R.id.globalActionToCommentDialog,
                Bundle().apply { putString("postId", comment.id) }
            )
        }
    }

    private fun subscribeToObservers() {

        basePostViewModel.likePostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                curLikedIndex?.let { index ->
                    postAdapter.posts[index].isLiking = false
                    postAdapter.notifyItemChanged(index)
                }
                snackbar(it)
            },
            onLoading = {
                curLikedIndex?.let { index ->
                    postAdapter.posts[index].isLiking = true
                    postAdapter.notifyItemChanged(index)
                }
            }
        ) { isLiked ->
            curLikedIndex?.let { index ->
                val uid = FirebaseAuth.getInstance().uid!!
                postAdapter.posts[index].apply {
                    this.isLiked = isLiked
                    isLiking = false
                    if(isLiked) {
                        likedBy += uid
                    } else {
                        likedBy -= uid
                    }
                }
                postAdapter.notifyItemChanged(index)
            }
        })

        basePostViewModel.deletePostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { deletedPost ->
            postAdapter.posts -= deletedPost
        })

        basePostViewModel.posts.observe(viewLifecycleOwner, EventObserver(
            onError = {
                postProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                postProgressBar.isVisible = true
            }
        ) { posts ->
            postProgressBar.isVisible = false
            postAdapter.posts = posts
        })

        basePostViewModel.likedByUsers.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { users ->
            val userAdapter = UserAdapter(glide)
            userAdapter.users = users
            LikedByDialog(userAdapter).show(childFragmentManager, null)
        })
    }
}
