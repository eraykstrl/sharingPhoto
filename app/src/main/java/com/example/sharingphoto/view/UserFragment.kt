package com.example.sharingphoto.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserFragment : Fragment() {

    private var _binding : FragmentUserBinding?= null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

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
        binding.signUpButton.setOnClickListener { signUp(it) }
        binding.signInButton.setOnClickListener { signIn(it) }

        val currentUser = auth.currentUser
        if(currentUser != null)
        {
            // user entered previously
            val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
            findNavController().navigate(action)
        }
    }

    fun signUp(view : View)
    {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if(email.isNotEmpty() &&  password.isNotEmpty())
        {
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                task->
                if(task.isSuccessful)
                {
                    val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
                    findNavController().navigate(action)   // user was created here
                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clear()
    {
        binding.emailText.setText("")
        binding.passwordText.setText("")
    }

    fun signIn(view : View)
    {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty())
        {
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
                findNavController().navigate(action)
            }.addOnFailureListener {
                exception->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
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