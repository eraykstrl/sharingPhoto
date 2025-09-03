package com.example.sharingphoto.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentSettingsBinding
import com.example.sharingphoto.viewmodel.SettingsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage


class SettingsFragment : Fragment() {

    private var _binding : FragmentSettingsBinding ?= null
    private val binding get() = _binding!!

    private lateinit var viewModel : SettingsViewModel
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        val currentUser = auth.currentUser
        val currentEmail = currentUser?.email

        if (currentUser != null) {
            binding.emailTextView.text = currentEmail.toString()

            binding.logOutTextView.setOnClickListener {
                viewModel.logOut()
            }

            observerLiveData()


            binding.homePageImageView.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToFeedFragment()
                updateUI(action)
            }

            binding.newPostImageView.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToUploadFragment()
                updateUI(action)
            }

            binding.newPasswordTextView.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToResetPasswordFragment()
                updateUI(action)
            }

            binding.aboutUsTextView.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment()
                updateUI(action)
            }

            binding.verifyEmailTextView.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToVerifyEmailFragment()
                updateUI(action)
            }

            binding.notificationTextView.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToNotificationFragment()
                updateUI(action)
            }

            binding.deleteAccountTextView.setOnClickListener {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("UYARI!!")
                alert.setMessage("Hesabı silmek istediğinize gerçekten emin misiniz eğer hesabınızı silerseniz tekrar geri alamazsınız!!")
                alert.setPositiveButton("Tamam") { dialog, which ->
                    it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    currentUser.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val alert = AlertDialog.Builder(requireContext())
                                alert.setMessage("Gittiğinize çok üzgünüz sizi tekrar bekliyoru<")

                                val action =
                                    SettingsFragmentDirections.actionSettingsFragmentToSignInFragment()
                                updateUI(action)
                                alert.show()

                            } else {
                                val alert = AlertDialog.Builder(requireContext())
                                alert.setTitle("Hata Oluştu")
                                alert.setMessage("Hesabı silerken bir hata oluştu lütfen tekrar deneyiniz")
                                alert.setPositiveButton("Tamam") { dialog, which ->
                                    dialog.dismiss()
                                }

                                alert.show()
                            }
                        }

                }

                alert.setNegativeButton("Hayır") { dialog, which ->
                    dialog.dismiss()
                }

                alert.show()
            }

            binding.reachUsTextView.setOnClickListener {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                val action = SettingsFragmentDirections.actionSettingsFragmentToReachUsFragment()
                updateUI(action)
            }

            binding.frequentlyQuestionsTextView.setOnClickListener {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                val action = SettingsFragmentDirections.actionSettingsFragmentToQuestionFragment()
                updateUI(action)
            }

        } else {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Giriş hatası oluştu")
            alert.setMessage("Lütfen tekrar giriş yapınız")
            alert.setPositiveButton("Tamam") { dialog, which ->
                dialog.dismiss()
                val action = SettingsFragmentDirections.actionSettingsFragmentToSignInFragment()
                updateUI(action)
            }
            alert.show()


        }

    }

    private fun observerLiveData()
    {
        viewModel.photoLiveData.observe(viewLifecycleOwner) {
            result->
            if(result == "success")
            {
                println("başarılı")
            }
            else
            {
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Hata oluştu")
                alert.setMessage(result.toString())
                alert.setPositiveButton("Tamam") {
                    dialog,which->
                    dialog.dismiss()
                }
                alert.show()
            }
        }

        viewModel.logOutLiveData.observe(viewLifecycleOwner) {
            result->
            if(result == 1)
            {
                val action = SettingsFragmentDirections.actionSettingsFragmentToSignInFragment()
                updateUI(action)
            }
        }

        viewModel.userFirstCharacterLiveData.observe(viewLifecycleOwner) {
            result->
            if(result !=1)
            {
                Snackbar.make(requireView(),"Ad, soyad bilgisi alınamıyor " +
                        "${result.toString()}", Snackbar.LENGTH_SHORT).show()
            }


        }

        viewModel.profilePhotoLiveData.observe(viewLifecycleOwner) {
            result->
            if(result != true || result != false)
            {
                Snackbar.make(requireView(),"Profil fotoğrafı bilgisi alınamıyor " +
                        "${result.toString()}", Snackbar.LENGTH_SHORT).show()
            }

        }

        viewModel.getDownloadUrlLiveData.observe(viewLifecycleOwner) {
            result->
            if(result != 1)
            {
                Snackbar.make(requireView(),"Ad, soyad bilgisi alınamıyor " +
                        "${result.toString()}", Snackbar.LENGTH_SHORT).show()
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