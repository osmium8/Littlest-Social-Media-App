package com.example.snplc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.snplc.R
import com.example.snplc.data.entities.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_post.view.*
import javax.inject.Inject

class PostAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * globals
         * if using kotlin synthetics, saves calls to findViewById, referenced once (good practice)
         */
        val ivPostImage: ImageView = itemView.ivPostImage
        val ivAuthorProfileImage: ImageView = itemView.ivAuthorProfileImage
        val tvPostAuthor: TextView = itemView.tvPostAuthor
        val tvPostText: TextView = itemView.tvPostText
        val tvLikedBy: TextView = itemView.tvLikedBy
        val ibLike: ImageButton = itemView.ibLike
        val ibComments: ImageButton = itemView.ibComments
        val ibDeletePost: ImageButton = itemView.ibDeletePost
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Post>() {
        /**
         * update only changed items in recylcer view
         */
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }
    }

    /**
     * list-differ : handles update process
     */
    private val differ = AsyncListDiffer(this, diffCallback)

    /**
     * list of posts to be displayed in recycler view
     */
    var posts: List<Post>
        get() = differ.currentList // the list of items in our recycler view
        set(value) = differ.submitList(value) // submitList, sets update list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_post,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.apply {

            glide.load(post.imageUrl).into(ivPostImage)
            glide.load(post.authorProfilePictureUrl).into(ivAuthorProfileImage)
            tvPostAuthor.text = post.authorUsername
            tvPostText.text = post.text

            val likeCount = post.likedBy.size
            tvLikedBy.text = when {
                likeCount <= 0 -> "No likes"
                likeCount == 1 -> "Liked by 1 person"
                else -> "Liked by $likeCount people"
            }

            val uid = FirebaseAuth.getInstance().uid!!
            ibDeletePost.isVisible = uid == post.authorUid
            // show delete post button
            // if logged-in user made that post

            ibLike.setImageResource(
                if(post.isLiked)
                    R.drawable.ic_like
                else
                    R.drawable.ic_like_border
            )

            /**
             * trigger onClickListeners
             */
            tvPostAuthor.setOnClickListener {
                onUserClickListener?.let { click ->
                    click(post.authorUid)
                }
            }
            ivAuthorProfileImage.setOnClickListener {
                onUserClickListener?.let { click ->
                    click(post.authorUid)
                }
            }
            tvLikedBy.setOnClickListener {
                onLikedByClickListener?.let { click ->
                    click(post)
                }
            }
            ibLike.setOnClickListener {
                onLikeClickListener?.let { click ->
                    if(!post.isLiking) click(post, holder.layoutPosition)
                }
            }
            ibComments.setOnClickListener {
                onCommentsClickListener?.let { click ->
                    click(post)
                }
            }
            ibDeletePost.setOnClickListener {
                onDeletePostClickListener?.let { click ->
                    click(post)
                }
            }
        }
    }

    private var onLikeClickListener: ((Post, Int) -> Unit)? = null
    private var onUserClickListener: ((String) -> Unit)? = null
    private var onDeletePostClickListener: ((Post) -> Unit)? = null
    private var onLikedByClickListener: ((Post) -> Unit)? = null
    private var onCommentsClickListener: ((Post) -> Unit)? = null

    fun setOnLikeClickListener(listener: (Post, Int) -> Unit) {
        onLikeClickListener = listener
    }

    fun setOnUserClickListener(listener: (String) -> Unit) {
        onUserClickListener = listener
    }

    fun setOnDeletePostClickListener(listener: (Post) -> Unit) {
        onDeletePostClickListener = listener
    }

    fun setOnLikedByClickListener(listener: (Post) -> Unit) {
        onLikedByClickListener = listener
    }

    fun setOnCommentsClickListener(listener: (Post) -> Unit) {
        onCommentsClickListener = listener
    }
}