package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.FollowingRequestAdapter
import com.example.sharingphoto.adapter.FriendRequestAdapter
import com.example.sharingphoto.databinding.FragmentShowYourRequestBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.viewmodel.ShowYourRequestViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.oAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ShowYourRequestFragment : Fragment() {

    private var _binding : FragmentShowYourRequestBinding ?= null
    private val binding get() = _binding!!

    private var followingRequestAdapter : FollowingRequestAdapter ?= null
    private val followingUserList : ArrayList<User> = arrayListOf()
    private var currentUser : FirebaseUser ?= null
    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel : ShowYourRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[ShowYourRequestViewModel::class]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentShowYourRequestBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = auth.currentUser
        if(currentUser != null)
        {

            val currentUserId = currentUser?.uid
            if(currentUserId != null)
            {
                getAdapter(currentUserId)
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getFollowingRequest(currentUserId)
                }

                observerLiveData()
            }
        }
    }


    private fun getAdapter(currentUserId : String)
    {
        followingRequestAdapter = FollowingRequestAdapter(followingUserList,

            cancelledRequest = {
                user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.removeUserFromFollowingRequest(currentUserId,user.user_id)

                }
            }
            )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = followingRequestAdapter
    }



    private fun observerLiveData()
    {
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            user ->
            followingRequestAdapter?.updateAdapter(user)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}