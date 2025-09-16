package com.example.sharingphoto.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.R
import com.example.sharingphoto.databinding.FragmentAccountSettingsBinding
import com.example.sharingphoto.viewmodel.AccountSettingsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.radians

class AccountSettingsFragment : Fragment() {

    private var _binding : FragmentAccountSettingsBinding ?= null
    private val binding get() = _binding!!

    private lateinit var viewModel : AccountSettingsViewModel
    private lateinit var auth : FirebaseAuth
    private var currentUser : FirebaseUser ?= null
    private var currentUserId : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[AccountSettingsViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentAccountSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = auth.currentUser
        if(currentUser != null)
        {
            currentUserId = currentUser?.uid
            if(currentUserId != null)
            {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getUserInfo(currentUserId)
                }

                binding.backIcon.setOnClickListener {
                    val action = AccountSettingsFragmentDirections.actionAccountSettingsFragment2ToSettingsFragment()
                    findNavController().navigate(action)
                }

                observerLiveData()
                binding.changeNameOrSurnameButton.setOnClickListener {
                    val name = binding.newNameEditText.text.toString()
                    val surname = binding.newSurnameEditText.text.toString()
                    if(name.isEmpty() && surname.isEmpty())
                    {
                        Snackbar.make(requireView(),"Lütfen ad veya soyad değiştirmek istediğiniz en az bir alana ait yeni bilgiyi giriniz",
                            Snackbar.LENGTH_SHORT).show()
                    }
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Uyarı")
                    alert.setMessage("Şu anda yaptığınız değişiklik doğrudan kaydedilecek devam etmek istediğinize emin misiniz?")
                    alert.setPositiveButton("Evet") {
                        dialog,which->
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.changeNameOrSurname(currentUserId,name,surname)

                        }
                    }
                    alert.setNegativeButton("Hayır") {
                        dialog,which->
                        dialog.dismiss()
                    }
                    alert.show()

                }

                binding.changeUsernameButton.setOnClickListener {
                    val username = binding.newUsernameEditText.text.toString()
                    if(username.isNullOrEmpty())
                    {
                        Snackbar.make(requireView(),"Kullanıcı adını değiştirmek istiyorsanız muhakkak yeni bir kullanıcı adı bilgisi girmelisiniz",
                            Snackbar.LENGTH_SHORT).show()
                    }
                    else
                    {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Uyarı")
                        alert.setMessage("Şu anda yaptığınız değişiklik doğrudan kaydedilecek devam etmek istediğinize emin misiniz?")
                        alert.setPositiveButton("Evet") {
                                dialog,which->
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.changeUsername(currentUserId,username)

                            }
                        }
                        alert.setNegativeButton("Hayır") {
                            dialog,which->
                            dialog.dismiss()
                        }
                        alert.show()
                    }
                }

                binding.changeAboutMeButton.setOnClickListener {
                    val aboutUser = binding.newAboutMeEditText.text.toString()
                    if(aboutUser.isNullOrEmpty())
                    {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Uyarı")
                        alert.setMessage("İşleme devam ederseniz hakkımda kısmı tamamen kaldırılacak")
                        alert.setPositiveButton("Tamam") {
                            dialog,which->
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.changeAboutUser(currentUserId,aboutUser)
                            }
                        }
                        alert.setNegativeButton("İptal") {
                            dialog,which->
                            dialog.dismiss()
                        }
                        alert.show()
                    }
                    else
                    {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Uyarı")
                        alert.setMessage("Şu anda yaptığınız değişiklik doğrudan kaydedilecek devam etmek istediğinize emin misiniz?")
                        alert.setPositiveButton("Evet") {
                            dialog,which->
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.changeAboutUser(currentUserId,aboutUser)

                            }
                        }
                        alert.setNegativeButton("Hayır") {
                            dialog,which->
                            dialog.dismiss()
                        }
                        alert.show()
                    }
                }

                binding.privacyTitle.setOnClickListener {
                    view ->
                    val popup = PopupMenu(view.context,view)
                    popup.menuInflater.inflate(R.menu.privacy_menu,popup.menu)
                    popup.setOnMenuItemClickListener {
                        item ->
                        when(item.itemId)
                        {
                            R.id.openItem -> {
                                binding.privacyTitle.text = "Herkese Açık"
                                binding.changePrivacyButton.setOnClickListener {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        viewModel.changePrivacy(currentUserId,0)

                                    }
                                }
                                true
                            }

                            R.id.hiddenItem -> {
                                binding.privacyTitle.text = "Gizli"
                                binding.changePrivacyButton.setOnClickListener {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        viewModel.changePrivacy(currentUserId,1)

                                    }
                                }
                                true
                            }
                            else -> {
                                false
                            }

                        }
                    }
                    popup.show()
                }
            }

        }

    }

    private fun observerLiveData()
    {

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            error ->
            if(error != false)
            {
                binding.secondLayout.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = error.toString()
            }
            else
            {
                binding.errorTextView.visibility = View.GONE
                binding.secondLayout.visibility = View.VISIBLE

            }
        }

        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
                result ->
            if(result)
            {
                binding.secondLayout.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
            else
            {
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.secondLayout.visibility = View.VISIBLE

            }
        }

        viewModel.userLiveData.observe(viewLifecycleOwner) {
            user ->
            binding.currentNameTextView.text = user.name
            binding.currentSurnameTextView.text = user.surname
            binding.usernameTextView.text = user.username
            binding.aboutMeTextView.text = user.aboutUser

            if(user.isPrivate != null && user.isPrivate)
            {
                binding.privacyTitle.text = "Gizli"
            }
            else
            {
                binding.privacyTitle.text = "Herkese Açık"
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}