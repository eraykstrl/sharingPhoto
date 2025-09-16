package com.example.sharingphoto.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.FriendRequestAdapter
import com.example.sharingphoto.databinding.FragmentFriendRequestBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.viewmodel.FriendRequestViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FriendRequestFragment : Fragment() {

    private var _binding : FragmentFriendRequestBinding ?= null
    private val binding get() = _binding!!
    private var requestAdapter : FriendRequestAdapter ?= null
    private val requestList : ArrayList<User> = arrayListOf()

    private lateinit var viewModel : FriendRequestViewModel
    private lateinit var auth : FirebaseAuth
    private var currentUser : FirebaseUser ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[FriendRequestViewModel::class]
        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentFriendRequestBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = auth.currentUser
        if(currentUser != null)
        {
            println("sanırım buraya girmedi")
            val currentUserId = currentUser?.uid
            requestAdapter()
            viewModel.getFollowerRequests(currentUserId!!)
            observerLiveData()

        }

    }



    private fun requestAdapter()
    {
        requestAdapter = FriendRequestAdapter(requestList,

            acceptClicked = {
                requestedId ->
                val currentUserId = currentUser?.uid
                if(currentUserId != null)
                {
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.acceptFollowerRequest(currentUserId,requestedId)

                    }
                }
                else
                {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Hata")
                    alert.setMessage("Kullanıcı bulunamadı")
                    alert.setPositiveButton("Tamam") {
                            dialog,which ->
                        dialog.dismiss()
                    }
                    alert.show()
                }

            },
            rejectClicked = {
                userId ->
                val currentUserId = currentUser?.uid
                if(currentUserId != null)
                {
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.rejectFollowerRequest(currentUserId,userId)

                    }
                }
                else
                {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Hata")
                    alert.setMessage("Kullanıcı bulunamadı")
                    alert.setPositiveButton("Tamam") {
                        dialog,which ->
                        dialog.dismiss()
                    }
                    alert.show()
                }

            }


        )
        binding.requestRecyclerView.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
        binding.requestRecyclerView.adapter = requestAdapter
    }


    private fun observerLiveData()
    {
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            list ->
            requestAdapter?.updateAdapter(list)

            if(list.isNotEmpty())
            {
                Log.d("boş degil","boş degil")
                binding.sadEmojiImageView.visibility = View.GONE
                binding.infoTextView.visibility = View.GONE
            }

            else
            {
                Log.d("boş","boş")
                binding.sadEmojiImageView.visibility = View.VISIBLE
                binding.infoTextView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}