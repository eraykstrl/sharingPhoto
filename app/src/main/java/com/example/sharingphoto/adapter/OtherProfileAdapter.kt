package com.example.sharingphoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.OtherRecyclerRowBinding
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.model.PostType
import com.example.sharingphoto.view.OtherProfileFragmentDirections
import com.example.sharingphoto.view.ProfileFragmentDirections
import com.google.android.material.transition.Hold

class OtherProfileAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<OtherProfileAdapter.OtherProfileHolder>()
{
    inner class OtherProfileHolder(val binding : OtherRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): OtherProfileHolder {

        val recyclerRowBinding = OtherRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OtherProfileHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: OtherProfileHolder,
        position: Int,
    ) {

        val post = postList[position]
        if(post.postType == PostType.FILE)
        {
            holder.binding.fileName.text = post.postName
        }

        else if (post.postType == PostType.TEXT)
        {
            holder.binding.fileName.text = post.userComment
            holder.binding.fileThumbnail.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToPersonalPostFragment(post.postId!!,post.user_id!!)
            it.findNavController().navigate(action)

        }
    }



    fun updateAdapter(newList : List<Post>)
    {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return postList.size
    }



}