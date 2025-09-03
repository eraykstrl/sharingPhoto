package com.example.sharingphoto.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentResetPasswordBinding
import com.example.sharingphoto.viewmodel.ResetPasswordViewModel
import com.google.android.material.snackbar.Snackbar


class ResetPasswordFragment : Fragment() {

    private var _binding : FragmentResetPasswordBinding ?= null
    private val binding get() = _binding!!
    private lateinit var viewModel : ResetPasswordViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ResetPasswordViewModel::class.java]



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentResetPasswordBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resetButton.setOnClickListener {
            resetPassword(it)
        }
        observerLiveData()
    }


    private fun observerLiveData()
    {
        viewModel.resetPasswordLiveData.observe(viewLifecycleOwner) {
            result->
            if(result == 1)
            {

                Snackbar.make(requireView(),"Başarılı bir şekilde şifreniz değiştirildi", Snackbar.LENGTH_SHORT).show()
                val action = ResetPasswordFragmentDirections.actionResetPasswordFragmentToSettingsFragment()
                updateUI(action)

            }
            else if (result == -1)
            {
                Snackbar.make(requireView(),"Eski şifrenizle yeni şifreniz aynı olamaz", Snackbar.LENGTH_SHORT).show()

            }
            else
            {
                Snackbar.make(requireView(),"Bir hata oluştu lütfen tekrar deneyiniz", Snackbar.LENGTH_SHORT).show()

            }
        }
    }

    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun resetPassword(view : View)
    {
        val oldPassword = binding.oldPasswordEditText.text.toString()
        val newPassword = binding.newPasswordEditText.text.toString()
        val newPasswordAgain = binding.newPasswordAgainEditText.text.toString()

        if(newPassword == newPasswordAgain)
        {
            viewModel.resetPassword(oldPassword,newPassword)

        }

        else
        {
            Snackbar.make(view,"Tekrar girdiğiniz şifreler eşleşmiyor lütfen tekrar deneyiniz",
                Snackbar.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}