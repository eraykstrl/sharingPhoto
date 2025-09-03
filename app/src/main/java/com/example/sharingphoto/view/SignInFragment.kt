package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentSignInBinding
import com.example.sharingphoto.viewmodel.SignInViewModel


class SignInFragment : Fragment() {

    private var _binding : FragmentSignInBinding ?= null
    private val binding get() = _binding!!

    private lateinit var viewModel : SignInViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentSignInBinding.inflate(inflater,container,false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observerLiveData()

        binding.signInButton.setOnClickListener {
            signIn(it)
        }

        binding.dontYouHaveAccount.setOnClickListener {
            val action = SignInFragmentDirections.actionSignInFragmentToUserFragment()
            updateUI(action)
        }

    }



    private fun signIn(view : View)
    {
        val email = binding.signInEmailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        viewModel.signIn(email,password)
    }

    private fun observerLiveData()
    {
        viewModel.signInLiveData.observe(viewLifecycleOwner) {
            user->
            user?.let {
                val action = SignInFragmentDirections.actionSignInFragmentToFeedFragment()
                updateUI(action)
            }
        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            error ->
            if(error != null)
            {
                val alert = android.app.AlertDialog.Builder(requireContext())
                alert.setTitle("Bir hata oluştu")
                alert.setMessage("Bir hata oluştu lütfen tekrar deneyiniz ${error}")
                alert.setPositiveButton("Tamam") {
                        dialog,which->
                    dialog.dismiss()
                }
                alert.show()
            }
        }
    }

    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}