package com.example.sharingphoto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.databinding.SendFriendRequestBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.view.SendFriendRequestFragmentDirections

class SendRequestAdapter(val userList : ArrayList<User>,
                        val followRequest : (User) -> Unit

) : RecyclerView.Adapter<SendRequestAdapter.SendRequestHolder>() {

    inner class SendRequestHolder(val binding: SendFriendRequestBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SendRequestHolder {

        val recyclerRowBinding = SendFriendRequestBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SendRequestHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: SendRequestHolder,
        position: Int,
    ) {
        val user = userList[position]
        holder.binding.requestRowUsername.text = user.username
        holder.binding.requestRowUsername.setOnClickListener {
            val action = SendFriendRequestFragmentDirections.actionSendFriendRequestFragmentToProfileFragment(user.user_id)
            it.findNavController().navigate(action)
        }

        holder.binding.requestAcceptButton.setOnClickListener {
            followRequest(user)
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }


    fun updateAdapter(newList: List<User>)
    {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

}