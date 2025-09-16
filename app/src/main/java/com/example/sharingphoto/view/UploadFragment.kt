package com.example.sharingphoto.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.R
import com.example.sharingphoto.databinding.FragmentUploadBinding
import com.example.sharingphoto.viewmodel.UploadViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.integrity.internal.ac
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class UploadFragment : Fragment() {

    private var _binding : FragmentUploadBinding?= null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncherVideo: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncherVideo : ActivityResultLauncher<String>

    private lateinit var activityResultLauncherFile : ActivityResultLauncher<Intent>

    private var selectedPicture : Uri? =null
    private var selectedBitmap  : Bitmap? =null

    private var selectedVideo : Uri ?= null

    private var selectedFile : Uri ?= null


    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var firestore : FirebaseFirestore
    private var currentUser : FirebaseUser ?= null

    private lateinit var viewModel : UploadViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        firestore = Firebase.firestore
        registerLaunchers()
        registerLauncherForVideo()
        registerLauncherForFile()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUploadBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[UploadViewModel::class.java]
        currentUser = auth.currentUser

        if(currentUser != null)
        {
            val userId = currentUser?.uid
            binding.uploadButton.setOnClickListener { uploadButton(userId,it) }
            binding.uploadPhotoImageView.setOnClickListener {
                    view ->
                val popup = PopupMenu(requireContext(),view)
                val menuInflater = popup.menuInflater
                menuInflater.inflate(R.menu.upload_videophoto_menu,popup.menu)

                popup.setOnMenuItemClickListener {
                        item ->
                    when(item.itemId) {

                        R.id.photoItem -> {
                            println("photo item cagrildi")
                            selectImage(view)
                            true
                        }

                        R.id.videoItem -> {
                            selectVideo(view)
                            true
                        }

                        else -> {
                            true
                        }
                    }
                }
                popup.show()
            }

            observerLiveData()

            binding.settingsImageView.setOnClickListener {
                val action = UploadFragmentDirections.actionUploadFragmentToSettingsFragment()
                updateUI(action)
            }
            binding.homePageImageView.setOnClickListener {
                val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                updateUI(action)
            }


            binding.uploadFileImageView.setOnClickListener {
                selectFile(it)
            }

            binding.btnRemoveFile.setOnClickListener {
                binding.fileLayout.visibility = View.GONE
                selectedFile = null
                binding.tvFileName.text  = ""
            }

            binding.profileImageView.setOnClickListener {
                val action = UploadFragmentDirections.actionUploadFragmentToProfileFragment(userId!!)
                updateUI(action)
            }

            binding.swipeRefreshLayout.setOnRefreshListener {
                binding.uploadFileImageView.visibility = View.GONE
                binding.uploadPhotoImageView.visibility = View.GONE
                binding.uploadButton.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.uploadFileImageView.visibility = View.VISIBLE
                binding.uploadPhotoImageView.visibility = View.VISIBLE
                binding.uploadButton.visibility = View.VISIBLE
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val name = viewModel.getName(userId)
                if(name != null)
                {
                    withContext(Dispatchers.Main) {
                        binding.commentText.hint = "Ne düşünüyorsun ${name}"
                    }
                }
                else
                {
                    withContext(Dispatchers.Main) {
                        binding.commentText.hint = "Ne düşünüyorsun?"
                    }
                }
            }

        }

    }

    private fun selectFile(view : View)
    {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        activityResultLauncherFile.launch(intent)
    }

    private fun registerLauncherForFile()
    {
        activityResultLauncherFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == Activity.RESULT_OK)
            {
                val intentFromResult = result.data
                if(intentFromResult != null)
                {
                    selectedFile = intentFromResult.data
                    if(selectedFile != null)
                    {
                        selectedFile?.let {
                            uri ->
                            val cursor = requireContext().contentResolver.query(uri,null,null,null,null)

                            cursor?.let {
                                if(it.moveToFirst())
                                {
                                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    val name = it.getString(index)
                                    binding.fileLayout.visibility = View.VISIBLE
                                    binding.tvFileName.text = name
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun uploadButton(userId : String?,view : View)
    {
        val reference = storage.reference
        val uuid = UUID.randomUUID()
        val imageName = "$uuid"
        val videoName ="$uuid"
        val fileName = "$uuid"
        val imagesReference = reference.child("images").child(imageName)
        val userComment = binding.commentText.text.toString()
        val videoReference = reference.child("videos").child(videoName)
        val fileReference = reference.child("files").child(fileName)

        if(selectedPicture != null)
        {
            binding.uploadButton.isEnabled = false
            imagesReference.putFile(selectedPicture!!).addOnSuccessListener {
                success->
                imagesReference.downloadUrl.addOnSuccessListener {
                    success->

                    if(auth.currentUser != null)
                    {
                        val downloadUrl = success.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            if(userId != null)
                            {
                                viewModel.setPostsByPhoto(userId,userComment,downloadUrl)

                            }
                        }
                    }
                }
                binding.uploadButton.isEnabled = true
            }
        }

        else if(selectedVideo != null)
        {
            binding.uploadButton.isEnabled = false
            println("video ekliyor olması lazım")
            videoReference.putFile(selectedVideo!!).addOnSuccessListener {
                success ->
                videoReference.downloadUrl.addOnSuccessListener {
                    success ->
                    val videoUrl = success.toString()
                    println("video url fragmentta ${videoUrl}")
                    lifecycleScope.launch(Dispatchers.IO) {
                        if(userId != null)
                        {
                            viewModel.setPostsByVideo(userId,userComment,videoUrl)
                        }
                    }

                }
                binding.uploadButton.isEnabled = false
            }.addOnFailureListener {
                e->
                println(e.message)
            }
        }

        else if(selectedFile != null)
        {
            var fileName = "unknown_file"
            val cursor = requireContext().contentResolver.query(selectedFile!!,null,null,null,null)
            cursor?.use {
                if(it.moveToFirst())
                {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(index != -1)
                    {
                        fileName = it.getString(index)
                    }
                }
            }

            val extension = fileName.substringAfterLast(".","").lowercase()
            if(extension != "pdf" && extension != "docx")
            {
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Hata")
                alert.setMessage("Bu dosya türü desteklenmiyor")
                alert.setPositiveButton("Tamam") {
                    dialog,which ->
                    dialog.dismiss()
                }

                alert.show()
            }
            else
            {
                binding.uploadButton.isEnabled = false
                fileReference.putFile(selectedFile!!).addOnSuccessListener {
                        success ->
                    fileReference.downloadUrl.addOnSuccessListener {
                            success ->
                        val fileUrl = success.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            if(userId != null)
                            {
                                viewModel.setPostsByFile(userId,userComment,fileUrl,fileName)
                            }
                        }

                    }
                    binding.uploadButton.isEnabled = true
                }
            }

        }

        else
        {
            lifecycleScope.launch(Dispatchers.IO) {
                if(userId != null)
                {
                    viewModel.setPosts(userId,userComment)

                }
            }
        }

    }

    private fun selectVideo(view : View)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_VIDEO))
                {
                    Snackbar.make(view,"Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"

                        , View.OnClickListener{
                            permissionLauncherVideo.launch(Manifest.permission.READ_MEDIA_VIDEO)
                        }).show()
                }
                else
                {
                    permissionLauncherVideo.launch(Manifest.permission.READ_MEDIA_VIDEO)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncherVideo.launch(intentToGallery)
            }
        }
        else
        {
             if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
             {
                 if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE))
                 {
                     Snackbar.make(view,"Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"

                         , View.OnClickListener{
                             permissionLauncherVideo.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                         }).show()
                 }
                 else
                 {
                     permissionLauncherVideo.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                 }

             }
            else
             {
                 val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                 activityResultLauncherVideo.launch(intentToGallery)
             }
        }
    }

    private fun selectImage(view : View)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
           if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
           {
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES))
                {
                    Snackbar.make(view,"Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"

                    , View.OnClickListener{
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                }

               else
                {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
           }

           else
           {
               val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
               activityResultLauncher.launch(intentToGallery)
               println(intentToGallery)
           }

        }

        else
        {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Snackbar.make(view,"Galeriye gitmemiz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                    , View.OnClickListener
                        {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }

                else
                {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
                println("activityresult launcher cagrildi")
                println(intentToGallery)
            }
        }
    }

    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun observerLiveData()
    {

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            error ->
            if(error != false)
            {
                println("error null değil")
                binding.uploadFileImageView.visibility = View.GONE
                binding.uploadPhotoImageView.visibility = View.GONE
                binding.uploadButton.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = error.toString()
            }
            else
            {
                println("error null")
                binding.errorTextView.visibility = View.GONE
                binding.uploadFileImageView.visibility = View.VISIBLE
                binding.uploadPhotoImageView.visibility = View.VISIBLE
                binding.uploadButton.visibility = View.VISIBLE

            }
        }

        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
            result ->
            if(result)
            {
                binding.uploadFileImageView.visibility = View.GONE
                binding.uploadPhotoImageView.visibility = View.GONE
                binding.uploadButton.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
            else
            {
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.uploadFileImageView.visibility = View.VISIBLE
                binding.uploadPhotoImageView.visibility = View.VISIBLE
                binding.uploadButton.visibility = View.VISIBLE

            }
        }

        viewModel.information.observe(viewLifecycleOwner) {
            result->
            if(result == 1)
            {
                val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                updateUI(action)
            }
        }
    }


    private fun registerLauncherForVideo()
    {
        activityResultLauncherVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == Activity.RESULT_OK)
            {
                val intentFromResult = result.data
                if(intentFromResult != null)
                {
                    selectedVideo = intentFromResult.data
                    if(selectedVideo != null)
                    {
                        binding.uploadElementLayout.visibility = View.GONE
                        val player = ExoPlayer.Builder(requireContext()).build()
                        binding.player.visibility = View.VISIBLE
                        binding.player.player = player
                        val mediaItem = MediaItem.fromUri(selectedVideo!!)
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                    }
                }
            }
        }

        permissionLauncherVideo = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            result ->
            if(result)
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncherVideo.launch(intentToGallery)
            }
            else
            {
                Toast.makeText(requireContext(),"İzni reddettiniz, izne ihtiyacımız var.", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun registerLaunchers()
    {
        println("register launchere girdi")
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            result->
            println("result cagrildi ${result}")
            if(result.resultCode == Activity.RESULT_OK)
            {
                val intentFromResult = result.data
                if(intentFromResult != null)
                {
                    selectedPicture = intentFromResult.data
                    println("selected picture var")
                    try
                    {
                        if(Build.VERSION.SDK_INT >= 28)
                        {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            println("bitmap seçildi niye buraya girmedi mi?")
                            binding.uploadElementLayout.visibility = View.GONE
                            binding.showPhotoImageView.visibility = View.VISIBLE
                            binding.showPhotoImageView.setImageBitmap(selectedBitmap)
                        }

                        else
                        {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
                            println("bitmap seçildi niye buraya girmedi mi?")
                            binding.uploadElementLayout.visibility = View.GONE
                            binding.showPhotoImageView.visibility = View.VISIBLE
                            binding.showPhotoImageView.setImageBitmap(selectedBitmap)
                        }
                    }

                    catch(e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        {
            result->
            if(result)
            {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

            else
            {
                Toast.makeText(requireContext(),"İzni reddettiniz, izne ihtiyacımız var.", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}