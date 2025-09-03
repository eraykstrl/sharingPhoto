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
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.PostAdapter
import com.example.sharingphoto.databinding.FragmentProfileBinding
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.path.fileVisitor


class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding ?= null
    private val binding get() = _binding!!
    private val postList : ArrayList<Post> = arrayListOf()
    private var postAdapter : PostAdapter ?=null
    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel : ProfileViewModel

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var selectedImage : Uri ?= null
    private var selectedBitmap : Bitmap ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val info = ProfileFragmentArgs.fromBundle(it).profileId
            val currentUser = auth.currentUser
            println("user id'si $info")
            if(currentUser != null)
            {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.getPosts(info)
                }
                postAdapter(info)

                observerLiveData()
                binding.profilePhotoImageView.setOnClickListener {
                    selectImage(it)
                }

                binding.homePageImageView.setOnClickListener {
                    val action = ProfileFragmentDirections.actionProfileFragmentToFeedFragment()
                    updateUI(action)
                }

                binding.newPostImageView.setOnClickListener {
                    val action = ProfileFragmentDirections.actionProfileFragmentToUploadFragment()
                    updateUI(action)
                }

                binding.settingsImageView.setOnClickListener {
                    val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
                    updateUI(action)
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getUser(info)
                }

            }
        }


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
                        }
                        else
                        {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedImage!!)
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



    private fun postAdapter(info : String)
    {
        val user_id = auth.currentUser?.uid
        postAdapter = PostAdapter(postList,

            recyclerViewVisibility =  {
                post ->
                val postId = post.postId
                val commentList = ArrayList<Comment>()
                commentList.addAll(post.comment)
                postAdapter?.showComments(postId!!,commentList)

            },
            onCommentClick =  {
                    comment ->
                if(comment.user_id == user_id)
                {
                    comment.isCommentOwner = true
                }

            },
            postUserId = {
                    post->
                val postOwnerId = post.user_id
                if(postOwnerId == user_id)
                {
                    post.isOwner = true
                }
            },
            sendComment = {
                    post,comment ->
                if(comment != "")
                {
//                    viewModel.setComment(post,comment)
                }
                else
                {
                    Snackbar.make(requireView(),"Yorum alanı boş bırakılamaz!", Snackbar.LENGTH_SHORT).show()
                }

            },

            updateComment = {
                    comment , post ->
                lifecycleScope.launch(Dispatchers.IO) {
//                    viewModel.updateOwnerComment(comment,post)
                }

            }
            , updateLike = {
                    post,info ->
                val userId = auth.currentUser?.uid
                lifecycleScope.launch(Dispatchers.IO) {
//                    viewModel.updateLike(post,info,userId!!)
                }
            },
            modifyComment = {
                    newComment,comment ->
                if(newComment != "")
                {
                    lifecycleScope.launch {
//                        viewModel.modifyComment(newComment,comment)
                    }

                }
                else
                {
                    Snackbar.make(requireView(),"Yorum alanı boş bırakılamaz!", Snackbar.LENGTH_SHORT).show()
                }
            }
            , openPopUpPost = {
                    holder,post,view ->
                val popup = PopupMenu(ContextThemeWrapper(view.context,R.style.PopUpMenuCustom),view)

                popup.menuInflater.inflate(R.menu.post_settings_menu,popup.menu)
                if(post.isOwner)
                {
                    popup.menu.findItem(R.id.deletePostItem).isVisible = true
                    popup.menu.findItem(R.id.reportPostItem).isVisible = true
                    popup.menu.findItem(R.id.sharePost).isVisible = true
                    popup.menu.findItem(R.id.modifyPost).isVisible = true
                    popup.menu.findItem(R.id.savePost).isVisible = true
                }

                else
                {
                    popup.menu.findItem(R.id.deletePostItem).isVisible = false
                    popup.menu.findItem(R.id.reportPostItem).isVisible = true
                    popup.menu.findItem(R.id.sharePost).isVisible = true
                    popup.menu.findItem(R.id.modifyPost).isVisible = false
                    popup.menu.findItem(R.id.savePost).isVisible = true
                }


                popup.setOnMenuItemClickListener {
                        item ->
                    when(item.itemId)
                    {
                        R.id.modifyPost -> {
                            holder.binding.savePostModify.visibility = View.VISIBLE
                            holder.binding.postOwnerComment.isEnabled = true
                            holder.binding.postOwnerComment.setSelection(holder.binding.postOwnerComment.text.length)
                            holder.binding.postOwnerComment.requestFocus()
                            true
                        }

                        R.id.sharePost -> {
                            val context = holder.itemView.context
                            val postLink = "https://github.com/eraykstrl?tab=repositories"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT,"Paylaşmak istediğiniz gönderi")
                                putExtra(Intent.EXTRA_TEXT,postLink)
                            }
                            context.startActivity(Intent.createChooser(shareIntent,"Gönderiyi paylaş"))
                            true
                        }

                        R.id.savePost ->
                        {
//                            val userId = user?.uid
//                            if(userId != null)
//                            {
//                                lifecycleScope.launch(Dispatchers.IO) {
////                                    viewModel.savePostId(post,userId)
//                                }
//                            }
                            true
                        }

                        R.id.deletePostItem -> {
                            lifecycleScope.launch(Dispatchers.IO) {
//                                viewModel.deletePost(post)
                            }
                            true
                        }

                        R.id.reportPostItem -> {
                            true
                        }

                        else ->
                        {
                            false
                        }
                    }
                }


                popup.show()

            },
            openPopUpComment = {
                    holder,comment,view->
                val popUp = PopupMenu(ContextThemeWrapper(view.context,R.style.PopUpMenuCustom),view)
                popUp.menuInflater.inflate(R.menu.comment_settings,popUp.menu)


                if(comment.isCommentOwner!!)
                {
                    popUp.menu.findItem(R.id.reportComment).isVisible = true
                    popUp.menu.findItem(R.id.deleteComment).isVisible = true
                    popUp.menu.findItem(R.id.modifyComment).isVisible = true
                }

                else
                {
                    popUp.menu.findItem(R.id.reportComment).isVisible = true

                }

                popUp.setOnMenuItemClickListener {
                        item ->
                    when(item.itemId)
                    {
                        R.id.modifyComment -> {
                            holder.binding.commentImageView.visibility = View.GONE
                            holder.binding.modifyCommentButton.visibility = View.VISIBLE
                            holder.binding.commentRecyclerViewEdit.isEnabled = true
                            holder.binding.commentRecyclerViewEdit.setSelection(holder.binding.commentRecyclerViewEdit.text.length)
                            holder.binding.commentRecyclerViewEdit.requestFocus()
                            true
                        }
                        R.id.deleteComment -> {
                            lifecycleScope.launch() {
//                                viewModel.deleteComment(comment)
                            }
                            true
                        }

                        R.id.reportComment -> {
                            true
                        }

                        else -> {
                            true
                        }
                    }
                }

                popUp.show()
            }

        )

        binding.profileRecyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        binding.profileRecyclerView.adapter = postAdapter
    }


    private fun observerLiveData()
    {
        viewModel.postLiveData.observe(viewLifecycleOwner) {
            posts ->
            postAdapter?.updateAdapter(posts)
        }

        viewModel.userLiveData.observe(viewLifecycleOwner) {
            user ->
            val builder = StringBuilder()
            println("user adı ${user.name}")
            builder.append(user.name)
            builder.append(" ")
            builder.append(user.surname)
            binding.nameTextView.text = builder.toString()
            Picasso.get().load(user.profilePhoto).into(binding.profilePhotoImageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}