package com.example.sharingphoto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingphoto.databinding.ProfilePostRowBinding
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.util.makePlaceHolder
import com.example.sharingphoto.view.VideoProfileFragmentDirections

class ProfileVideoAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<ProfileVideoAdapter.ProfileVideoHolder>() {
    inner class ProfileVideoHolder(val binding : ProfilePostRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ProfileVideoHolder {

        val recyclerRowBinding = ProfilePostRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProfileVideoHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: ProfileVideoHolder,
        position: Int,
    ) {
        val post = postList[position]

        if(post.videoUrl != null)
        {
            Glide.with(holder.itemView.context).asBitmap().load(post.videoUrl).frame(1000000).placeholder(
                makePlaceHolder(holder.itemView.context)
            ).into(holder.binding.recyclerImageView)
        }

        holder.itemView.setOnClickListener {
            val action = VideoProfileFragmentDirections.actionVideoProfileFragmentToPersonalPostFragment(post.postId!!,post.user_id!!)
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