package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.UserFollowersAdapter
import com.example.sharingphoto.databinding.FragmentUserFollowersBinding
import com.example.sharingphoto.databinding.FragmentUserFollowingBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.viewmodel.UserFollowersViewModel
import com.example.sharingphoto.viewmodel.UserFollowingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserFollowingFragment : Fragment() {

    private var _binding : FragmentUserFollowingBinding ?= null
    private val binding get () = _binding!!
    private var currentUser : FirebaseUser ?= null
    private lateinit var auth : FirebaseAuth
    private var currentUserId : String ?= null

    private var userFollowersAdapter : UserFollowersAdapter ?=null
    private val userFollowersList : ArrayList<User> = arrayListOf()
    private val state : ArrayList<Int> = arrayListOf()
    private lateinit var viewModel : UserFollowingViewModel
    private var info : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[UserFollowingViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentUserFollowingBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = auth.currentUser
        arguments?.let {
            info = UserFollowingFragmentArgs.fromBundle(it).userid
            if(currentUser != null)
            {
                currentUserId = currentUser?.uid
                if(currentUserId != null)
                {
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.getFollowingById(currentUserId!!,info!!)
                    }
                    getAdapter()
                    observerLiveData()

                    binding.backIcon.setOnClickListener {
                        val action = UserFollowingFragmentDirections.actionUserFollowingFragmentToProfileFragment(info!!)
                        findNavController().navigate(action)
                    }
                }
            }
        }

    }


    private fun getAdapter()
    {
        userFollowersAdapter = UserFollowersAdapter(userFollowersList,state,
            operationInfo = {
                    user,operation ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.userOperations(info,user.user_id,operation)

                }
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = userFollowersAdapter
    }


    private fun observerLiveData()
    {
        viewModel.userListLiveData.observe(viewLifecycleOwner) {
                list ->
            userFollowersAdapter?.updateAdapter(list.map {it.first},list.map { it.second })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}