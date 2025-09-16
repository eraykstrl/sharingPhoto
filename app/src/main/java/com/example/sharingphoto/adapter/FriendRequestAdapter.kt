package com.example.sharingphoto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.FragmentFriendRequestBinding
import com.example.sharingphoto.databinding.FriendRequestRowBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.view.FriendRequestFragmentDirections

class FriendRequestAdapter(val requestList : ArrayList<User>,
    val rejectClicked : (String) -> Unit,val acceptClicked : (String) -> Unit

) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestHolder>() {

    inner class FriendRequestHolder(val binding : FriendRequestRowBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FriendRequestHolder {

        val recyclerRowBinding = FriendRequestRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FriendRequestHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: FriendRequestHolder,
        position: Int,
    ) {
        val request = requestList[position]

        holder.binding.requestRowUsername.text = request.username
        holder.binding.requestRowUsername.setOnClickListener {
            val action = FriendRequestFragmentDirections.actionFriendRequestFragmentToProfileFragment(request.user_id)
            it.findNavController().navigate(action)
        }

        holder.binding.requestAcceptButton.setOnClickListener {
            acceptClicked(request.user_id)

        }

        holder.binding.requestRejectButton.setOnClickListener {
            rejectClicked(request.user_id)
        }


    }

    fun updateAdapter(newList : List<User>)
    {
        requestList.clear()
        requestList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return requestList.size
    }


}