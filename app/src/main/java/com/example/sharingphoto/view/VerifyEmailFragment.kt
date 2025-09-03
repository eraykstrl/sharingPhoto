package com.example.sharingphoto.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.R
import com.example.sharingphoto.databinding.FragmentVerifyEmailBinding
import com.example.sharingphoto.viewmodel.VerifyEmailViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VerifyEmailFragment : Fragment() {

    private var _binding : FragmentVerifyEmailBinding?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel : VerifyEmailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentVerifyEmailBinding.inflate(inflater,container,false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[VerifyEmailViewModel::class.java]
        observerLiveData()

        val currentUser = auth.currentUser
        val email = currentUser?.email
        if(currentUser == null)
        {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Giriş hatası oluştu")
            alert.setMessage("Lütfen tekrar giriş yapınız")
            alert.setPositiveButton("Tamam") {
                    dialog,which ->
                dialog.dismiss()
            }
            alert.show()

            val action = VerifyEmailFragmentDirections.actionVerifyEmailFragmentToSignInFragment()
            updateUI(action)
        }

        else
        {
            val infoText = readTextFile(R.raw.mail_info)

            binding.emailTextView.text = email.toString()
            binding.verifyButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.sendEmailVerificationCode(currentUser)
                }
            }



        }

    }


    private fun readTextFile(resourceId : Int) : String
    {
        return resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }


    private fun observerLiveData()
    {
        viewModel.verifyLiveData.observe(viewLifecycleOwner) {
            result->
            if(result == 1)
            {
                Snackbar.make(requireView(),"Email adresinize doğrulamak için bir link gönderildi lütfen" +
                        "doğrulama işlemini tamamlayınız",Snackbar.LENGTH_LONG).show()

            }
            else
            {
                Snackbar.make(requireView(),result.toString(), Snackbar.LENGTH_LONG).show()
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