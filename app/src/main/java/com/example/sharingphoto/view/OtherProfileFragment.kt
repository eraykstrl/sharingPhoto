package com.example.sharingphoto.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.OtherProfileAdapter
import com.example.sharingphoto.databinding.FragmentOtherProfileBinding
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.util.downloadImage
import com.example.sharingphoto.util.makePlaceHolder
import com.example.sharingphoto.viewmodel.OtherProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class OtherProfileFragment : Fragment() {

    private var _binding : FragmentOtherProfileBinding ?= null
    private val binding get() = _binding!!

    private var otherProfileAdapter : OtherProfileAdapter ?= null
    private val otherPostList : ArrayList<Post> = arrayListOf()

    private var currentUser : FirebaseUser ?= null
    private var info : String ?= null
    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel : OtherProfileViewModel
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var selectedImage : Uri ?= null
    private var selectedBitmap : Bitmap ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[OtherProfileViewModel::class.java]
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentOtherProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = auth.currentUser
        if(currentUser != null)
        {
            arguments?.let {
                info = OtherProfileFragmentArgs.fromBundle(it).userId
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getOthersFromInternet(info!!)
                }

                binding.swipeRefreshLayout.setOnRefreshListener {
                    binding.profileRecyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.getOthersFromInternet(info!!)
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getFollowingInfo(info)

                }
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getUser(info!!)
                }

                observerLiveData()
                getAdapter()
                binding.imagesImageView.setOnClickListener {
                    val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToProfileFragment(info!!)
                    updateUI(action)
                }

                binding.videosImageView.setOnClickListener {
                    val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToVideoProfileFragment(info!!)
                    updateUI(action)
                }

                binding.followButton.setOnClickListener {
                    val colorInt = (binding.root.background as? ColorDrawable)?.color
                    val hexColor = String.format("#%06X", 0xFFFFFF and (colorInt ?: 0))
                    val amberCustom = ContextCompat.getColor(requireContext(),R.color.amber_custom)
                    if(hexColor == "#CFD8DC")
                    {
                        binding.followButton.setBackgroundColor(amberCustom)
                    }
                    else
                    {
                        binding.followButton.setBackgroundColor(Color.WHITE)
                    }

                    viewModel.followingRequest(info)
                }

                binding.profilePhotoImageView.setOnClickListener {
                    selectImage(it)
                }
            }
        }

    }


    private fun getAdapter()
    {
        otherProfileAdapter = OtherProfileAdapter(otherPostList)
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profileRecyclerView.adapter = otherProfileAdapter
    }


    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun setProfilePhoto()
    {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.setProfilePhoto(selectedImage)
        }
    }
    private fun selectImage(view : View)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES ) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES))
                {
                    Snackbar.make(view,"Galeriden fotoğraf seçmek için izin vermeniz gerekiyor!",
                        Snackbar.LENGTH_INDEFINITE).setAction("Tamam",
                        View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }

                else
                {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else
            {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }
        }
        else
        {
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Snackbar.make(view,"Galeriden fotoğraf seçmek için izin vermeniz gerekiyor!",Snackbar.LENGTH_INDEFINITE).setAction("Tamam"
                        , View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }
                else
                {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else
            {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }
        }



    }

    private fun registerLauncher()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result->
            if(result.resultCode == Activity.RESULT_OK)
            {
                val intentFromResult = result.data
                if(intentFromResult != null)
                {
                    selectedImage = intentFromResult.data
                    if(selectedImage != null)
                    {
                        Snackbar.make(requireView(),
                            "Fotoğraf eklemek istediğinize emin misiniz? Değişiklikleri kaydedin.",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Tamam") {
                            setProfilePhoto()
                        }.show()
                    }

                    try
                    {
                        if(Build.VERSION.SDK_INT >= 28)
                        {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.profilePhotoImageView.setImageBitmap(selectedBitmap)
                        }
                        else
                        {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedImage!!)
                            binding.profilePhotoImageView.setImageBitmap(selectedBitmap)

                        }
                    }

                    catch (e : Exception)
                    {
                        e.printStackTrace()
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Hata")
                        alert.setMessage("Bir hata oluştu lütfen tekrar deneyiniz")
                        alert.setPositiveButton("Tamam") {
                                dialog,which ->
                            dialog.dismiss()
                        }
                        alert.show()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                result ->
            if(result)
            {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }
            else
            {
                Snackbar.make(requireView(),"İzni reddettiniz izne ihtiyacımız var", Snackbar.LENGTH_INDEFINITE).show()


            }
        }
    }

    private fun observerLiveData()
    {
        viewModel.postLiveData.observe(viewLifecycleOwner) {
                posts ->
            otherProfileAdapter?.updateAdapter(posts)
        }

        viewModel.userLiveData.observe(viewLifecycleOwner) {
                user ->
            binding.followerNumberTextView.visibility = View.VISIBLE
            binding.followingNumberTextView.visibility = View.VISIBLE
            binding.aboutEditText.setText(user.aboutUser)
            if(user.profilePhoto.isNullOrEmpty())
            {
                binding.profilePhotoImageView.visibility = View.VISIBLE
            }
            else
            {
                binding.profilePhotoImageView.downloadImage(user.profilePhoto, makePlaceHolder(requireContext()))
            }
            val builder = StringBuilder()
            builder.append(user.name)
            builder.append(" ")
            builder.append(user.surname)
            binding.nameTextView.text = builder.toString()
            var followerCount : Int =0
            var followingCount : Int =0
            followerCount = if(user.follower.isEmpty()) 0 else user.follower.size
            followingCount = if(user.following.isEmpty()) 0 else user.following.size
            binding.followerNumberTextView.text = followerCount.toString()
            binding.followingNumberTextView.text = followingCount.toString()
            if(user.aboutUser != null || user.aboutUser != "")
            {
                binding.aboutEditText.setText(user.aboutUser)

            }


            binding.followerTextView.setOnClickListener {
                if(followerCount != 0)
                {
                    if(findNavController().currentDestination?.id == R.id.otherProfileFragment)
                    {
                        val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToUserFollowersFragment(user.user_id)
                        updateUI(action)
                    }

                }
                else
                {
                    Snackbar.make(requireView(),"Görüntülenecek kullanıcı yok", Snackbar.LENGTH_SHORT).show()

                }

            }
            binding.followerNumberTextView.setOnClickListener {
                if(followerCount != 0)
                {
                    if(findNavController().currentDestination?.id == R.id.otherProfileFragment)
                    {
                        val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToUserFollowersFragment(user.user_id)
                        updateUI(action)
                    }
                }
                else
                {
                    Snackbar.make(requireView(),"Görüntülenecek kullanıcı yok", Snackbar.LENGTH_SHORT).show()

                }
            }



            binding.followingNumberTextView.setOnClickListener {
                if(followingCount != 0)
                {
                    if(findNavController().currentDestination?.id == R.id.otherProfileFragment)
                    {
                        val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToUserFollowingFragment(user.user_id)
                        updateUI(action)
                    }
                }
                else
                {
                    Snackbar.make(requireView(),"Görüntülenecek kullanıcı yok", Snackbar.LENGTH_SHORT).show()

                }
            }

            binding.followingTextView.setOnClickListener {
                if(followingCount != 0)
                {
                    if(findNavController().currentDestination?.id == R.id.otherProfileFragment)
                    {
                        val action = OtherProfileFragmentDirections.actionOtherProfileFragmentToUserFollowingFragment(user.user_id)
                        updateUI(action)
                    }
                }
                else
                {
                    Snackbar.make(requireView(),"Görüntülenecek kullanıcı yok", Snackbar.LENGTH_SHORT).show()

                }
            }

        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
                error ->
        }

        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
                it ->
            if(it)
            {
                binding.errorTextView.visibility = View.GONE
                binding.profileRecyclerView.visibility = View.GONE
                binding.followerNumberTextView.visibility = View.GONE
                binding.followingNumberTextView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE

            }
            else
            {
                binding.progressBar.visibility = View.GONE

            }

        }


        viewModel.followingRequestLiveData.observe(viewLifecycleOwner) {
                (user,data) ->
            binding.aboutEditText.apply {
                isFocusable = false
                isFocusableInTouchMode = false
                isCursorVisible = false
            }
            val customColor = ContextCompat.getColor(requireContext(),R.color.button_back_color)
            val customTextColor = ContextCompat.getColor(requireContext(),R.color.amber_custom)
            if(data == 1 && user.isPrivate != false)
            {
                binding.lockImageView.visibility = View.VISIBLE
                binding.followButton.text = "Takip Et"
                binding.profileRecyclerView.visibility = View.GONE
                binding.followButton.setBackgroundColor(customColor)
                binding.followButton.setTextColor(customTextColor)
                binding.followButton.setOnClickListener {
                    binding.followButton.text = "Takip İsteği Gönderildi"
                    binding.followButton.setBackgroundColor(Color.WHITE)
                    binding.followButton.setTextColor(Color.BLACK)
                    viewModel.addToFollowRequest(user.user_id)
                }
            }
            else if(data == 2 && user.isPrivate != false)
            {
                binding.lockImageView.visibility = View.VISIBLE
                binding.followButton.text = "Takip İsteği Gönderildi"
                binding.profileRecyclerView.visibility = View.GONE
                binding.followButton.setBackgroundColor(Color.WHITE)
                binding.followButton.setTextColor(Color.BLACK)
                binding.followButton.setOnClickListener {
                    binding.followButton.text = "Takip Et"
                    binding.followButton.setBackgroundColor(customColor)
                    binding.followButton.setTextColor(customTextColor)
                    viewModel.removeFromFollowRequest(user.user_id)
                }
            }
            else if(data == 3) {
                binding.lockImageView.visibility = View.GONE
                binding.profileRecyclerView.visibility = View.VISIBLE
                binding.followButton.text = "Takip Ediliyor"
                binding.followButton.setBackgroundColor(Color.WHITE)
                binding.followButton.setTextColor(Color.BLACK)
                binding.followButton.setOnClickListener {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Uyarı")
                    alert.setMessage("Takipten çıkmak istediğinize emin misiniz tekrar takip isteği atmanız gerekecek")
                    alert.setPositiveButton("Evet") {
                            dialog, which ->

                        binding.followButton.setBackgroundColor(customColor)
                        binding.followButton.setTextColor(customTextColor)
                        viewModel.removeFromFollowing(user.user_id)
                        binding.followButton.text = "Takip Et"
                        binding.profileRecyclerView.visibility = View.GONE
                    }
                    alert.setNegativeButton("İptal") {
                            dialog,which ->
                        dialog.dismiss()
                    }
                    alert.show()

                }
            }
            else
            {
                binding.followButton.visibility = View.GONE
                binding.profileRecyclerView.visibility = View.VISIBLE


            }


        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}