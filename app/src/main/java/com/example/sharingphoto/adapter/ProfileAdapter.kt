package com.example.sharingphoto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.ProfilePostRowBinding
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.model.PostType
import com.example.sharingphoto.util.downloadImage
import com.example.sharingphoto.util.makePlaceHolder
import com.example.sharingphoto.view.ProfileFragmentDirections

class ProfileAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<ProfileAdapter.ProfileHolder>() {
    inner class ProfileHolder(val binding : ProfilePostRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ProfileHolder {

        val recyclerRow = ProfilePostRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProfileHolder(recyclerRow)
    }

    override fun onBindViewHolder(
        holder: ProfileHolder,
        position: Int,
    ) {
        val post = postList[position]
        val downloadUrl = post.downloadUrl

        holder.binding.recyclerImageView.downloadImage(downloadUrl!!,makePlaceHolder(holder.itemView.context))

        holder.itemView.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToPersonalPostFragment(post.postId!!,post.user_id!!)
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