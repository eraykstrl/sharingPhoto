package com.example.sharingphoto.adapter

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.RecyclerCommentRowBinding
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CommentAdapter
    (       val commentList : ArrayList<Comment>,
            val onCommentClick : (Comment) -> Unit,
            val modifyComment : (String,Comment) -> Unit,
            val openPopUpComment : (CommentHolder, Comment, View) -> Unit

) : RecyclerView.Adapter<CommentAdapter.CommentHolder> (){



    class CommentHolder(val binding : RecyclerCommentRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CommentHolder {

        val recyclerCommentBinding = RecyclerCommentRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CommentHolder(recyclerCommentBinding)
    }

    override fun onBindViewHolder(
        holder: CommentHolder,
        position: Int,
    ) {

        val comment = commentList[position]
        holder.binding.commentRecyclerViewText.text = comment.username.toString()
        holder.binding.commentRecyclerViewEdit.setText(comment.comment.toString())


        holder.binding.commentImageView.setOnClickListener {
            view ->
            onCommentClick(comment)
            openPopUpComment(holder,comment,view)

        }


        holder.binding.modifyCommentButton.setOnClickListener {
            val text = holder.binding.commentRecyclerViewEdit.text.toString()
            modifyComment(text,comment)
            holder.binding.commentRecyclerViewEdit.isEnabled = false
            holder.binding.modifyCommentButton.visibility = View.GONE
            holder.binding.commentImageView.visibility = View.VISIBLE
        }

    }

    fun updateComment(comment: Comment)
    {
        val position = commentList.indexOfFirst { it.commentId == comment.commentId }
        println("pozisyom $position")
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {

        return commentList.size
    }

    fun updateComments(newComment : MutableList<Comment>)
    {
        commentList.clear()
        commentList.addAll(newComment)
        notifyDataSetChanged()

    }



}