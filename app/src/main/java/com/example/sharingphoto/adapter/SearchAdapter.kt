package com.example.sharingphoto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.SearchRowBinding
import com.example.sharingphoto.util.downloadImage
import com.example.sharingphoto.util.makePlaceHolder
import com.example.sharingphoto.view.DiscoverFragmentDirections

class SearchAdapter(private val searchList : ArrayList<Triple<String, String, String?>>) : RecyclerView.Adapter<SearchAdapter.SearchHolder>() {
    inner class SearchHolder(val binding : SearchRowBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchHolder {

        val recyclerRowBinding = SearchRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SearchHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: SearchHolder,
        position: Int,
    ) {
        val userId = searchList[position].first
        val username = searchList[position].second
        val profile = searchList[position].third
        holder.binding.username.text = username
        if(profile != null)
        {
            holder.binding.userImage.downloadImage(profile, makePlaceHolder(holder.itemView.context))
        }

        holder.itemView.setOnClickListener {
            val action = DiscoverFragmentDirections.actionDiscoverFragment2ToProfileFragment(userId)
            it.findNavController().navigate(action)
        }

    }

    fun updateAdapter(newList : List<Triple<String, String,String?>>)
    {
        println("update adapter cagrildi")
        searchList.clear()
        searchList.addAll(newList)
        notifyDataSetChanged()
    }



    override fun getItemCount(): Int {
        return searchList.size
    }

}