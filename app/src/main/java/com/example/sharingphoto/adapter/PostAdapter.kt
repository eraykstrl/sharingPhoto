package com.example.sharingphoto.adapter
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.RecyclerRowBinding
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.adapter.CommentAdapter.CommentHolder
import com.example.sharingphoto.databinding.RecyclerFileRowBinding
import com.example.sharingphoto.databinding.RecyclerTextRowBinding
import com.example.sharingphoto.databinding.RecyclerVideoRowBinding
import com.example.sharingphoto.model.PostType
import com.example.sharingphoto.util.downloadImage
import com.example.sharingphoto.util.makePlaceHolder


class PostAdapter(
    private val postList: ArrayList<Post>,
    private val recyclerViewVisibility: (Post) -> Unit,
    private val onCommentClick : (Comment) -> Unit,
    private val postUserId : (Post) -> Unit,
    private val sendComment : (Post,String) -> Unit,
    private val updateComment : (String,Post) -> Unit,
    private val updateLike : (Post,Int) -> Unit,
    private val modifyComment : (String,Comment) -> Unit,
    private val openPopUpComment :(CommentHolder, Comment, View) -> Unit,
    private val openPopUpPost : (PostHolder,Post, View) -> Unit,
    private val clickPost :(String) -> Unit,
    private val clickUsername : (String) -> Unit,
    private val fileClicked : (String,String) -> Unit,

    ) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

    private val commentList : ArrayList<Comment> =arrayListOf()
    private var commentAdapters = mutableMapOf<String, CommentAdapter>()
    private var player : ExoPlayer ?= null

    private val viewPool = RecyclerView.RecycledViewPool()

    init {
        setHasStableIds(true)
    }

    sealed class PostHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        class ImageHolder(val binding : RecyclerRowBinding) : PostHolder(binding.root)
        class VideoHolder(val binding : RecyclerVideoRowBinding)  : PostHolder(binding.root)
        class TextHolder(val binding : RecyclerTextRowBinding) : PostHolder(binding.root)
        class FileHolder(val binding : RecyclerFileRowBinding) : PostHolder(binding.root)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PostHolder {

        return when(viewType)
        {
            0-> {
                val recyclerRow = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                PostHolder.ImageHolder(recyclerRow)
            }

            1 -> {
                val recyclerRow = RecyclerVideoRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                PostHolder.VideoHolder(recyclerRow)
            }

            2 -> {
                val recyclerRow = RecyclerFileRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                PostHolder.FileHolder(recyclerRow)
            }

            3 -> {
                val recyclerRow = RecyclerTextRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                PostHolder.TextHolder(recyclerRow)
            }

            else -> {
                val recyclerRow = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                PostHolder.ImageHolder(recyclerRow)
            }
        }


    }

    fun prepareCommentsRecycler(recycler : RecyclerView,postId: String)
    {
        if(recycler.adapter == null)
        {
            val adapter = CommentAdapter(arrayListOf(), onCommentClick, modifyComment, openPopUpComment)
            commentAdapters[postId] = adapter

            recycler.apply {
                isNestedScrollingEnabled = false
                setRecycledViewPool(viewPool)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
                setHasFixedSize(true)
                this.adapter = adapter
            }
        }
        else
        {
            recycler.setRecycledViewPool(viewPool)
            recycler.isNestedScrollingEnabled = false
        }
    }

    override fun onBindViewHolder(
        holder: PostHolder,
        position: Int,
    ) {
        val post = postList[position]

        when (holder) {
            is PostHolder.ImageHolder -> imageHolder(holder, post, position)
            is PostHolder.VideoHolder -> videoHolder(holder, post, position)
            is PostHolder.FileHolder -> fileHolder(holder,position,post)
            is PostHolder.TextHolder -> textHolder(holder, post, position)
        }

    }

    private fun imageHolder(holder: PostHolder.ImageHolder,post: Post,position : Int)
    {

        holder.binding.recyclerImageView.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickPost(userId)
            }
        }

        prepareCommentsRecycler(holder.binding.secondRecyclerView,post.postId!!)

        holder.binding.usernameLinearLayout.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickUsername(userId)
            }
        }

        holder.binding.numberCommentsTextView.text = post.commentCounter.toString()
        holder.binding.numberLikeTextView.text = post.likeCounter.toString()

        manageGeneralViews(holder,position)


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


    private fun textHolder(holder : PostHolder.TextHolder , post : Post,position: Int)
    {
        holder.binding.postTextView.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickPost(userId)
            }
        }
        val adapter = commentAdapters.getOrPut(post.postId!!) {
            CommentAdapter(commentList,onCommentClick,modifyComment,openPopUpComment)
        }
        holder.binding.secondRecyclerView.adapter = adapter
        holder.binding.secondRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context,
            LinearLayoutManager.VERTICAL,
            false
        )

        manageGeneralViews(holder,position)

        holder.binding.nameTextView.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickUsername(userId)
            }
        }

        holder.binding.numberCommentsTextView.text = post.commentCounter.toString()
        holder.binding.numberLikeTextView.text = post.likeCounter.toString()


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


    private fun fileHolder(holder : PostHolder.FileHolder,position: Int,post : Post)
    {
        holder.binding.fileGeneralLayout.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickPost(userId)
            }
        }
        prepareCommentsRecycler(holder.binding.secondRecyclerView,post.postId!!)


        holder.binding.nameTextView.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickUsername(userId)
            }
        }

        manageGeneralViews(holder,position)

        holder.binding.numberCommentsTextView.text = post.commentCounter.toString()
        holder.binding.numberLikeTextView.text = post.likeCounter.toString()



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

        holder.binding.fileNameLayout.setOnClickListener {
            fileClicked(postList[position].postName!!,postList[position].fileUrl!!)

        }
    }

    private fun videoHolder(holder : PostHolder.VideoHolder,post : Post,position: Int)
    {
        holder.binding.recyclerPlayer.player = null

        val context = holder.itemView.context
        player = ExoPlayer.Builder(context).build().also {
            exoPlayer ->
            holder.binding.recyclerPlayer.player = exoPlayer

            if(post.videoUrl != null)
            {
                val mediaItem = MediaItem.fromUri(post.videoUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false
            }
        }



        holder.binding.recyclerPlayer.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickPost(userId)
            }
        }
        prepareCommentsRecycler(holder.binding.secondRecyclerView,post.postId!!)


        holder.binding.nameTextView.setOnClickListener {
            post.user_id?.let {
                    userId ->
                clickUsername(userId)
            }
        }

        manageGeneralViews(holder,position)

        holder.binding.numberCommentsTextView.text = post.commentCounter.toString()
        holder.binding.numberLikeTextView.text = post.likeCounter.toString()



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
            println("bastin begeniye")
            val post = postList[position]
            if(post.isLiked)
            {
                println("begenilmis")
                updateLike(post,1)
                holder.binding.likeImageView.setColorFilter(Color.TRANSPARENT)

            }
            else
            {
                println("begenilmemis buraya girdi")
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


    private fun manageGeneralViews(holder : PostHolder,position : Int)
    {
        val post = postList[position]
        when (holder) {
            is PostHolder.ImageHolder -> {
                val context = holder.itemView.context

                holder.binding.nameTextView.text = post.username?.trim()
                holder.binding.postOwnerComment.setText(post.userComment?.trim())
                holder.binding.sendingNumberTextView.text = post.sendNumber.toString()
                holder.binding.recyclerImageView.downloadImage(post.downloadUrl!!, makePlaceHolder(context))
                holder.binding.numberLikeTextView.text = post.likeCounter.toString()
                holder.binding.numberCommentsTextView.text = post.commentCounter.toString()
            }
            is PostHolder.VideoHolder -> {
                holder.binding.nameTextView.text = post.username?.trim()
                holder.binding.postOwnerComment.setText(post.userComment?.trim())
                holder.binding.sendingNumberTextView.text = post.sendNumber.toString()
            }
            is PostHolder.TextHolder -> {
                holder.binding.sendingNumberTextView.text = post.sendNumber.toString()
                holder.binding.postTextView.text = post.userComment?.trim()
                holder.binding.nameTextView.text = post.username.toString()
            }
            is PostHolder.FileHolder -> {
                holder.binding.nameTextView.text = post.username?.trim()
                holder.binding.postOwnerComment.setText(post.userComment?.trim())
                holder.binding.sendingNumberTextView.text = post.sendNumber.toString()
                holder.binding.fileName.text = post.postName
            }
        }
    }


    override fun getItemCount(): Int {
        return postList.size
    }

    fun updateAdapter(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return postList[position].postId?.hashCode()?.toLong() ?:position.toLong()
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


    override fun getItemViewType(position: Int): Int {
        return when(postList[position].postType) {
            PostType.IMAGE -> 0
            PostType.VIDEO -> 1
            PostType.FILE -> 2
            PostType.TEXT -> 3
            else -> 0
        }

    }




}
