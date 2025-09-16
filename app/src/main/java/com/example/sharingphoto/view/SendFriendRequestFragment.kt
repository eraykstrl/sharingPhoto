package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.SendRequestAdapter
import com.example.sharingphoto.databinding.FragmentFriendRequestBinding
import com.example.sharingphoto.databinding.FragmentSendFriendRequestBinding
import com.example.sharingphoto.model.User
import com.example.sharingphoto.viewmodel.SendFriendRequestViewModel
import com.google.api.Distribution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SendFriendRequestFragment : Fragment() {

    private var _binding : FragmentSendFriendRequestBinding ?= null
    private val binding get() = _binding!!
    private var sendRequestAdapter : SendRequestAdapter ?= null
    private val userList : ArrayList<User> = arrayListOf()
    private lateinit var viewModel : SendFriendRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SendFriendRequestViewModel::class]


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSendFriendRequestBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("girdik fragmenta istek gönderme fragmentayiz")
        getAdapter()
        viewModel.getAllUser()
        observerLiveData()

        binding.backIcon.setOnClickListener {
            val action = SendFriendRequestFragmentDirections.actionSendFriendRequestFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun getAdapter()
    {
        println("adapter cagirliyor")
        sendRequestAdapter = SendRequestAdapter(userList ,

            followRequest = {
                user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.setFollower(user.user_id)
                }
            }
        )
        binding.userRecyclerView.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
        binding.userRecyclerView.adapter = sendRequestAdapter
        println(sendRequestAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun observerLiveData()
    {
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            list ->
            sendRequestAdapter?.updateAdapter(list)
            if(list.isNotEmpty())
            {
                binding.winkingImageView.visibility = View.GONE
                binding.infoTextView.visibility = View.GONE
            }
            else
            {

                binding.winkingImageView.visibility = View.VISIBLE
                binding.infoTextView.visibility = View.VISIBLE
            }

        }
    }


}