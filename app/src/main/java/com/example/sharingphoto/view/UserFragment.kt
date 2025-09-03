package com.example.sharingphoto.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentUserBinding
import com.example.sharingphoto.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserFragment : Fragment() {

    private var _binding : FragmentUserBinding?= null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth

    private lateinit var viewModel : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUserBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signUpButton.setOnClickListener {   signUp(it) }

        binding.youHaveAccount.setOnClickListener {
            val action = UserFragmentDirections.actionUserFragmentToSignInFragment()
            updateUI(action)
        }

        observerLiveData()

    }

    private fun signUp(view : View)
    {
        val name = binding.nameEditText.text.toString()
        val surname = binding.surnameEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordText.text.toString()
        viewModel.signUp(name,surname,username,email,password)
    }

    private fun observerLiveData()
    {
        viewModel.signUpLiveData.observe(viewLifecycleOwner) {
            user ->
            user?.let {
                val action = UserFragmentDirections.actionUserFragmentToSignInFragment()
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


    fun clear()
    {
        binding.emailEditText.setText("")
        binding.passwordText.setText("")
    }



    override fun onResume() {
        super.onResume()
        clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }


}