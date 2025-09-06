package com.example.sharingphoto.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentUploadBinding
import com.example.sharingphoto.viewmodel.UploadViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class UploadFragment : Fragment() {

    private var _binding : FragmentUploadBinding?= null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>

    private var selectedPicture : Uri? =null
    private var selectedBitmap  : Bitmap? =null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var firestore : FirebaseFirestore

    private lateinit var viewModel : UploadViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        firestore = Firebase.firestore

        registerLaunchers()
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

        binding.uploadButton.setOnClickListener { uploadButton(it) }
        binding.uploadPhotoImageView.setOnClickListener { selectImage(it) }

        observerLiveData()

        binding.settingsImageView.setOnClickListener {
            val action = UploadFragmentDirections.actionUploadFragmentToSettingsFragment()
            updateUI(action)
        }
        binding.homePageImageView.setOnClickListener {
            val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
            updateUI(action)
        }


    }



    fun uploadButton(view : View)
    {
        val reference = storage.reference
        val uuid = UUID.randomUUID()
        val imageName = "$uuid"
        val imagesReference = reference.child("images").child(imageName)
        val userComment = binding.commentText.text.toString()


        if(selectedPicture != null)
        {
            imagesReference.putFile(selectedPicture!!).addOnSuccessListener {
                success->
                imagesReference.downloadUrl.addOnSuccessListener {
                    success->

                    if(auth.currentUser != null)
                    {
                        val downloadUrl = success.toString()

                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.setPosts(userComment,downloadUrl)

                        }

                    }

                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()

            }
        }
    }



    fun selectImage(view : View)
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
                        })
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
            }
        }
    }

    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun observerLiveData()
    {
        viewModel.information.observe(viewLifecycleOwner) {
            result->
            if(result == 1)
            {
                val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                updateUI(action)
            }
            else
            {
                println(result)
            }
        }
    }

    private fun registerLaunchers()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            result->
            if(result.resultCode == Activity.RESULT_OK)
            {
                val intentFromResult = result.data
                if(intentFromResult != null)
                {
                    selectedPicture = intentFromResult.data
                    try
                    {
                        if(Build.VERSION.SDK_INT >= 28)
                        {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                        }

                        else
                        {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
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