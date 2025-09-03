package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sharingphoto.databinding.FragmentNotificationBinding
import com.example.sharingphoto.viewmodel.NotificationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotificationFragment : Fragment() {


    private var _binding : FragmentNotificationBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    private var messageChecked : Boolean ?= null
    private var applicationChecked : Boolean ?= null
    private var likeChecked : Boolean ?= null
    private var commentChecked : Boolean ?= null
    private var securityChecked : Boolean ?= null
    private var followingChecked : Boolean ?= null

    private lateinit var viewModel : NotificationViewModel

    private var stateMap = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentNotificationBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        observerLiveData()

        val currentUser = auth.currentUser

        if(currentUser != null)
        {
            getChangeListener()
            binding.saveChangesButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.changeNotificationSettings(currentUser,stateMap)
                }
            }

        }

        else
        {

        }


    }



    private fun getChangeListener()
    {

        binding.messageSwitch.setOnCheckedChangeListener { _, isChecked ->
            messageChecked = isChecked
            updateMap("messageNotification",messageChecked!!)
        }

        binding.applicationSwitch.setOnCheckedChangeListener { _, isChecked ->
            applicationChecked = isChecked
            updateMap("applicationNotification",applicationChecked!!)
        }


        binding.likeSwitch.setOnCheckedChangeListener { _, isChecked ->
            likeChecked = isChecked
            updateMap("likeNotification",likeChecked!!)
        }


        binding.commentSwitch.setOnCheckedChangeListener { _, isChecked ->
            commentChecked= isChecked
            updateMap("commentNotification",commentChecked!!)
        }

        binding.securitySwitch.setOnCheckedChangeListener {
            _, isChecked ->
            securityChecked= isChecked
            updateMap("securityNotification",securityChecked!!)
        }

        binding.followSwitch.setOnCheckedChangeListener {
            _, isChecked ->
            followingChecked= isChecked
            updateMap("followingNotification",followingChecked!!)
        }

    }

    private fun observerLiveData()
    {
        viewModel.notificationLiveData.observe(viewLifecycleOwner)
        {
            result->
            if(result == 1)
            {
                Snackbar.make(requireView(),"Tercihleriniz başarılı bir şekilde kaydedildi",
                    Snackbar.LENGTH_SHORT).show()
            }
            else
            {
                Snackbar.make(requireView(),result.toString(),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateMap(name : String,state : Boolean)
    {

        stateMap.put(name,state)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}