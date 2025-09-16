package com.example.sharingphoto.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingphoto.R
import com.example.sharingphoto.databinding.UserFollowersRowBinding
import com.example.sharingphoto.model.User


class UserFollowersAdapter(private val userFollowersList : ArrayList<User>,private var stateList : ArrayList<Int>,
                            private val operationInfo : (User,Int) -> Unit

) : RecyclerView.Adapter<UserFollowersAdapter.UserFollowersHolder>() {
    inner class UserFollowersHolder(val binding : UserFollowersRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): UserFollowersHolder {

        val recyclerRowBinding = UserFollowersRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserFollowersHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(
        holder: UserFollowersHolder,
        position: Int,
    ) {
        println("adaptere girdik")
        val user = userFollowersList[position]
        val state =stateList[position]

        println("followers ${userFollowersList[position].username}")
        holder.binding.requestRowUsername.text = user.username
        println("adapterdeyiz ve user adı ${user.username}")

        if(state == 1)
        {
            holder.binding.requestAcceptButton.text = "Takip Et"
            val context = holder.itemView.context
            val customColor = ContextCompat.getColor(context,R.color.button_back_color)
            val customTextColor = ContextCompat.getColor(context,R.color.amber_custom)
            holder.binding.requestAcceptButton.setBackgroundColor(customColor)
            holder.binding.requestAcceptButton.setTextColor(customTextColor)
            holder.binding.requestAcceptButton.setOnClickListener {
                holder.binding.requestAcceptButton.text = "Takip İsteği Gönderildi"
                holder.binding.requestAcceptButton.setBackgroundColor(Color.WHITE)
                holder.binding.requestAcceptButton.setTextColor(Color.BLACK)
                operationInfo(user,0)
            }
        }
        else if(state == 2)
        {
            val context = holder.itemView.context
            val customColor = ContextCompat.getColor(context,R.color.button_back_color)
            val customTextColor = ContextCompat.getColor(context,R.color.amber_custom)
            holder.binding.requestAcceptButton.text = "Takip İsteği Gönderildi"
            holder.binding.requestAcceptButton.setBackgroundColor(Color.WHITE)
            holder.binding.requestAcceptButton.setTextColor(Color.BLACK)
            holder.binding.requestAcceptButton.setOnClickListener {
                holder.binding.requestAcceptButton.text = "Takip Et"
                holder.binding.requestAcceptButton.setBackgroundColor(customColor)
                holder.binding.requestAcceptButton.setTextColor(customTextColor)
                operationInfo(user,1)
            }

        }
        else if(state == 3)
        {
            holder.binding.requestAcceptButton.text = "Takip Ediliyor"
            val context = holder.itemView.context
            val customColor = ContextCompat.getColor(context,R.color.button_back_color)
            val customTextColor = ContextCompat.getColor(context,R.color.amber_custom)
            holder.binding.requestAcceptButton.setBackgroundColor(Color.WHITE)
            holder.binding.requestAcceptButton.setTextColor(Color.BLACK)
            holder.binding.requestAcceptButton.setOnClickListener {
                val alert = AlertDialog.Builder(context)
                alert.setTitle("Uyarı")
                alert.setMessage("Eğer onaylarsanız kullanıcıyı takip etmek için tekrar istek göndermeniz gerekecek")
                alert.setPositiveButton("Tamam") {
                    dialog,which->
                    holder.binding.requestAcceptButton.text = "Takip Et"
                    holder.binding.requestAcceptButton.setBackgroundColor(customColor)
                    holder.binding.requestAcceptButton.setTextColor(customTextColor)
                    operationInfo(user,2)
                }
                alert.setNegativeButton("İptal") {
                    dialog,which->
                    dialog.dismiss()
                }
                alert.show()
            }
        }
        else
        {
            holder.binding.requestAcceptButton.visibility = View.GONE

        }

    }


    fun updateAdapter(newList : List<User>,newState : List<Int>)
    {
        userFollowersList.clear()
        stateList.clear()
        stateList.addAll(newState)
        userFollowersList.addAll(newList)
        newList.forEach {
            println("user adi ${it.username}")
        }
        newState.forEach {
            println("user durumu ${it}")
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userFollowersList.size
    }



}