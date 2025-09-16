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
import com.example.sharingphoto.databinding.FragmentSettingsBinding
import com.example.sharingphoto.viewmodel.SettingsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {

    private var _binding : FragmentSettingsBinding ?= null
    private val binding get() = _binding!!

    private lateinit var viewModel : SettingsViewModel
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage : FirebaseStorage

    private var currentUser : FirebaseUser ?= null
    private var currentUserId : String ?= null
    private var currentEmail : String ?= null

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

        currentUser = auth.currentUser

        if (currentUser != null)
        {
            currentEmail = currentUser?.email
            currentUserId = currentUser?.uid
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
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.deleteAllInfo(currentUserId!!)
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
            binding.newFriendTextView.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToSendFriendRequestFragment()
                updateUI(action)
            }

            binding.followerRequest.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToFriendRequestFragment()
                updateUI(action)
            }

            binding.sentFollowRequest.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToShowYourRequestFragment()
                updateUI(action)
            }
            binding.profileImageView.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToProfileFragment(currentUserId!!)
                updateUI(action)
            }

            binding.accountSettingsTextView.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToAccountSettingsFragment2()
                updateUI(action)
            }

            binding.savedPost.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToSavedPostsFragment()
                updateUI(action)
            }

        }
        else
        {
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

        viewModel.logOutLiveData.observe(viewLifecycleOwner) {
            result->
            if(result == 1)
            {
                val action = SettingsFragmentDirections.actionSettingsFragmentToSignInFragment()
                updateUI(action)
            }
        }

        viewModel.deleteLiveData.observe(viewLifecycleOwner) {
            result ->
            if(result)
            {
                currentUser?.delete()
                    ?.addOnCompleteListener { task ->
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