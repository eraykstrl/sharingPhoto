package com.example.sharingphoto.adapter
import android.content.Intent
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.RecyclerRowBinding
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.squareup.picasso.Picasso
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.CommentAdapter.CommentHolder
import com.example.sharingphoto.view.FeedFragmentDirections

class PostAdapter(
    val postList: ArrayList<Post>,
    val recyclerViewVisibility: (Post) -> Unit,
    val onCommentClick : (Comment) -> Unit,
    val postUserId : (Post) -> Unit,
    val sendComment : (Post,String) -> Unit,
    val updateComment : (String,Post) -> Unit,
    val updateLike : (Post,Int) -> Unit,
    val modifyComment : (String,Comment) -> Unit,
    val openPopUpComment :(CommentHolder, Comment, View) -> Unit,
    val openPopUpPost : (PostHolder,Post, View) -> Unit,

    ) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

    private val commentList : ArrayList<Comment> =arrayListOf()
    private var commentAdapters = mutableMapOf<String, CommentAdapter>()
    private var counter : Int =0


    class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PostHolder {
        val recyclerRowBinding =
            RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(recyclerRowBinding)

    }

    override fun onBindViewHolder(
        holder: PostHolder,
        position: Int,
    ) {

        holder.binding.likeImageView.setOnClickListener {

        }


        val post = postList[position]
        val adapter = commentAdapters.getOrPut(post.postId!!) {
            CommentAdapter(commentList,onCommentClick,modifyComment,openPopUpComment)
        }
        holder.binding.secondRecyclerView.adapter = adapter
        holder.binding.secondRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context,
            LinearLayoutManager.VERTICAL,
            false
        )

        holder.binding.usernameLinearLayout.setOnClickListener {
            val userId = post.user_id
            if(userId != null)
            {
                val action = FeedFragmentDirections.actionFeedFragmentToProfileFragment(userId)
                it.findNavController().navigate(action)
            }
        }

        holder.binding.numberCommentsTextView.text = post.commentCounter.toString()
        holder.binding.numberLikeTextView.text = post.likeCounter.toString()

        manageGeneralViews(holder.binding,position)


        holder.binding.savePostModify.setOnClickListener {
            val comment = holder.binding.postOwnerComment.text.toString().trim()
            updateComment(comment,postList[position])
            holder.binding.postOwnerComment.isEnabled = false
            holder.binding.savePostModify.visibility = View.GONE
        }
        if(post.isLiked)
        {
            holder.binding.likeImageView.setColorFilter(Color.RED)

        }
        else
        {
            holder.binding.likeImageView.setColorFilter(Color.TRANSPARENT)
        }

        holder.binding.likeImageView.setOnClickListener {
            val post = postList[position]
            if(post.isLiked)
            {
                updateLike(post,1)
                holder.binding.likeImageView.setColorFilter(Color.TRANSPARENT)

            }
            else
            {
                updateLike(post,0)
                holder.binding.likeImageView.setColorFilter(Color.RED)
            }
        }


        holder.binding.commentImageView.setOnClickListener {

            val isVisible= holder.binding.secondRecyclerView.isVisible
            val visibilitySpecials= if (isVisible) View.GONE else View.VISIBLE
            holder.binding.secondRecyclerView.visibility = visibilitySpecials
            holder.binding.commentEditText.visibility = visibilitySpecials
            holder.binding.commentSendButton.visibility = visibilitySpecials
            recyclerViewVisibility(postList[position])

        }

        holder.binding.postSettingsImageView.setOnClickListener {
            view ->
            postUserId(post)
            openPopUpPost(holder,post,view)

        }


        holder.binding.commentSendButton.setOnClickListener {
            val comment = holder.binding.commentEditText.text.toString()
            sendComment(post, comment)
            holder.binding.commentEditText.setText("")
            recyclerViewVisibility(postList[position])
        }

    }



    fun showComments(postId: String,newList : MutableList<Comment>)
    {
        val adapter = commentAdapters[postId] ?: return
        adapter.updateComments(newList)

    }


    private fun manageGeneralViews(binding : RecyclerRowBinding,position : Int)
    {
        val post = postList[position]
        binding.nameTextView.text = post.username?.trim()
        binding.postOwnerComment.setText(post.userComment?.trim())
        binding.sendingNumberTextView.text = postList[position].sendNumber.toString()
        println(post.downloadUrl)
        Picasso.get().load(postList[position].downloadUrl).into(binding.recyclerImageView)
    }


    override fun getItemCount(): Int {
        return postList.size
    }

    fun updateAdapter(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updatePost(post : Post)
    {
        val position = postList.indexOfFirst { it.postId == post.postId }
        if(position != -1)
        {
            postList[position].comment = post.comment
            postList[position].likeCounter = post.likeCounter
            postList[position].commentCounter = post.commentCounter
            notifyItemChanged(position)

        }
    }


}
