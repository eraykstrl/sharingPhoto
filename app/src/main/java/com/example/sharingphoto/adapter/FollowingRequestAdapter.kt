package com.example.sharingphoto.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.FriendRequestRowBinding
import com.example.sharingphoto.databinding.ShowFollowingRequestRowBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.view.ShowYourRequestFragmentDirections
import com.google.android.play.integrity.internal.ac

class FollowingRequestAdapter(
    private val followingList : ArrayList<User>,
    private val cancelledRequest : (User) -> Unit



) : RecyclerView.Adapter<FollowingRequestAdapter.FollowingRequestHolder>() {

    inner class FollowingRequestHolder(val binding : ShowFollowingRequestRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FollowingRequestHolder {
        val recyclerRowBinding = ShowFollowingRequestRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FollowingRequestHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: FollowingRequestHolder,
        position: Int,
    ) {
        val request = followingList[position]
        holder.binding.requestRowUsername.text = request.username
        holder.binding.requestRowUsername.setOnClickListener {
            val action = ShowYourRequestFragmentDirections.actionShowYourRequestFragmentToProfileFragment(request.user_id)
            it.findNavController().navigate(action)
        }

        holder.binding.requestRejectButton.setOnClickListener {
            holder.binding.requestRejectButton.setBackgroundColor(Color.WHITE)
            holder.binding.requestRejectButton.setTextColor(Color.BLACK)
            cancelledRequest(request)
        }
    }


    fun updateAdapter(newList : List<User>)
    {
        followingList.clear()
        followingList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return followingList.size
    }



}