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
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.databinding.FragmentUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class UploadFragment : Fragment() {

    private var _binding : FragmentUploadBinding?= null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>

    var selectedPicture : Uri? =null
    var selectedBitmap  : Bitmap? =null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

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
        binding.uploadButton.setOnClickListener { uploadButton(it) }
        binding.imageView.setOnClickListener { selectImage(it) }
        binding.commentText.setOnClickListener {  }
    }


    fun uploadButton(view : View)
    {
        val reference = storage.reference
        val uuid = UUID.randomUUID()
        val imageName = "$uuid"
        val imagesReference = reference.child("images").child(imageName)


        if(selectedPicture != null)
        {
            imagesReference.putFile(selectedPicture!!).addOnSuccessListener {
                success->
                imagesReference.downloadUrl.addOnSuccessListener {
                    success->

                    if(auth.currentUser != null)
                    {
                        val downloadUrl = success.toString()

                        val postMap = hashMapOf<String, Any>()
                        postMap.put("downloadUrl",downloadUrl)
                        postMap.put("email",auth.currentUser!!.email.toString())
                        postMap.put("comment",binding.commentText.text.toString())
                        postMap.put("date", Timestamp.now())

                        db.collection("Posts").add(postMap).addOnSuccessListener {
                            success->

                            val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                            findNavController().navigate(action)

                        }.addOnFailureListener {
                            exception->
                            Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
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
                        //izin istememiz lazım
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                }

               else
                {
                    //izin istememiz lazım
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
           }

           else
           {
               // izin var galeriye gidebiliriz
               val intentToGallery =
                   Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            // read external storage
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
                            binding.imageView.setImageBitmap(selectedBitmap)

                        }

                        else
                        {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
                            binding.imageView.setImageBitmap(selectedBitmap)
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
                // izin verildi
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

            else
            {
                Toast.makeText(requireContext(),"İzni reddettiniz, izne ihtiyacımız var.", Toast.LENGTH_LONG).show()
                // izin verilmedi
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}