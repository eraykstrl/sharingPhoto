package com.example.sharingphoto.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.RecyclerRowBinding
import com.example.sharingphoto.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(val postList : List<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>()
{
    class PostHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)
    {

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PostHolder {

        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: PostHolder,
        position: Int,
    ) {

        holder.binding.recyclerEmailText.text = postList[position].email
        holder.binding.recyclerCommentText.text = postList[position].comment

        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.recyclerImageView)
    }

    override fun getItemCount(): Int {
        return postList.size
    }


}