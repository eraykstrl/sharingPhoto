package com.example.sharingphoto.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.PostAdapter
import com.example.sharingphoto.adapter.SearchAdapter
import com.example.sharingphoto.databinding.FragmentDiscoverBinding
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.viewmodel.DiscoverViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.api.Distribution
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.io.File


class DiscoverFragment : Fragment() {

    private var _binding : FragmentDiscoverBinding ?= null
    private val binding get() = _binding!!

    private var postAdapter : PostAdapter ?= null
    private val postList : ArrayList<Post> = arrayListOf()
    private lateinit var viewModel : DiscoverViewModel
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage

    private var currentuser  : FirebaseUser ?= null
    private var currentUserId : String ?= null
    private var stateFlow = MutableStateFlow("")

    private var searchAdapter : SearchAdapter ?=null
    private val searchList : ArrayList<Triple<String, String,String?>> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DiscoverViewModel::class.java]
        auth = Firebase.auth
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentDiscoverBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.following.setTextColor(Color.GRAY)
        binding.discover.setTextColor(Color.BLACK)
        currentuser = auth.currentUser
        if(currentuser != null)
        {
            currentUserId = currentuser?.uid
            viewModel.getPosts()

            adapter()
            binding.newPostImageView.setOnClickListener {
                val action = DiscoverFragmentDirections.actionDiscoverFragment2ToUploadFragment()
                updateUI(action)
            }
            binding.profileImageView.setOnClickListener {
                val action = DiscoverFragmentDirections.actionDiscoverFragment2ToProfileFragment(currentUserId!!)
                updateUI(action)
            }
            binding.settingsImageView.setOnClickListener {
                val action = DiscoverFragmentDirections.actionDiscoverFragment2ToSettingsFragment()
                updateUI(action)
            }

            binding.following.setOnClickListener {
                val action = DiscoverFragmentDirections.actionDiscoverFragment2ToFeedFragment()
                updateUI(action)
            }

            binding.searchInputEditText.addTextChangedListener {
                input ->
                if(input.isNullOrEmpty())
                {
                    binding.searchingRecyclerView.visibility = View.GONE
                }
                else
                {
                    binding.searchingRecyclerView.visibility = View.VISIBLE
                    println("boş degil")
                    stateFlow.value = input.toString()
                }
            }

            observerLiveData()

            lifecycleScope.launch(Dispatchers.IO) {
                stateFlow.debounce(400).distinctUntilChanged().filter { it.isNotEmpty() }.collect {
                    viewModel.search(it)
                }
            }
        }
    }


    private fun adapter()
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
                    viewModel.setComment(post,comment)
                }
                else
                {
                    Snackbar.make(requireView(),"Yorum alanı boş bırakılamaz!", Snackbar.LENGTH_SHORT).show()
                }

            },

            updateComment = {
                    comment , post ->
                viewModel.updateOwnerComment(comment,post)


            }
            , updateLike = {
                    post,info ->
                val userId = auth.currentUser?.uid
                viewModel.updateLike(post,info,userId!!)

            },
            modifyComment = {
                    newComment,comment ->
                if(newComment != "")
                {
                    viewModel.modifyComment(newComment,comment)

                }
                else
                {
                    Snackbar.make(requireView(),"Yorum alanı boş bırakılamaz!", Snackbar.LENGTH_SHORT).show()
                }
            }
            , openPopUpPost = { holder, post, view ->

                val popup = PopupMenu(ContextThemeWrapper(view.context, R.style.PopUpMenuCustom), view)
                popup.menuInflater.inflate(R.menu.post_settings_menu, popup.menu)

                if(post.isOwner) {
                    popup.menu.findItem(R.id.deletePostItem).isVisible = true
                    popup.menu.findItem(R.id.reportPostItem).isVisible = true
                    popup.menu.findItem(R.id.sharePost).isVisible = true
                    popup.menu.findItem(R.id.modifyPost).isVisible = true
                    popup.menu.findItem(R.id.savePost).isVisible = true
                } else {
                    popup.menu.findItem(R.id.deletePostItem).isVisible = false
                    popup.menu.findItem(R.id.reportPostItem).isVisible = true
                    popup.menu.findItem(R.id.sharePost).isVisible = true
                    popup.menu.findItem(R.id.modifyPost).isVisible = false
                    popup.menu.findItem(R.id.savePost).isVisible = true
                }

                when(holder)
                {
                    is PostAdapter.PostHolder.ImageHolder -> {
                        val b = holder.binding
                        popup.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.modifyPost -> {
                                    b.savePostModify.visibility = View.VISIBLE
                                    b.postOwnerComment.isEnabled = true
                                    b.postOwnerComment.setSelection(b.postOwnerComment.text.length)
                                    b.postOwnerComment.requestFocus()
                                    true
                                }

                                R.id.sharePost -> {
                                    val context = holder.itemView.context
                                    val postLink =
                                        "https://github.com/eraykstrl?tab=repositories"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Paylaşmak istediğiniz gönderi"
                                        )
                                        putExtra(Intent.EXTRA_TEXT, postLink)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            shareIntent,
                                            "Gönderiyi paylaş"
                                        )
                                    )
                                    true
                                }

                                R.id.savePost -> {
                                    currentuser?.uid?.let { userId : String->
                                        viewModel.savePost(post, userId)
                                    }
                                    true
                                }

                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
                                    true
                                }

                                R.id.reportPostItem -> true
                                else -> false
                            }
                        }
                    }

                    is PostAdapter.PostHolder.VideoHolder -> {
                        val b = holder.binding
                        popup.setOnMenuItemClickListener { item ->
                            when(item.itemId) {
                                R.id.modifyPost -> {
                                    b.savePostModify.visibility = View.VISIBLE
                                    b.postOwnerComment.isEnabled = true
                                    b.postOwnerComment.setSelection(b.postOwnerComment.text.length)
                                    b.postOwnerComment.requestFocus()
                                    true
                                }
                                R.id.sharePost -> {
                                    val context = holder.itemView.context
                                    val postLink = "https://github.com/eraykstrl?tab=repositories"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT,"Paylaşmak istediğiniz gönderi")
                                        putExtra(Intent.EXTRA_TEXT, postLink)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent,"Gönderiyi paylaş"))
                                    true
                                }
                                R.id.savePost -> {
                                    currentuser?.uid?.let { userId ->
                                        viewModel.savePost(post, userId)
                                    }
                                    true
                                }
                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
                                    true
                                }
                                R.id.reportPostItem -> true
                                else -> false
                            }
                        }
                    }

                    is PostAdapter.PostHolder.TextHolder -> {
                        val b = holder.binding
                        popup.setOnMenuItemClickListener { item ->
                            when(item.itemId) {
                                R.id.modifyPost -> {
                                    b.savePostModify.visibility = View.VISIBLE
                                    b.postOwnerComment.isEnabled = true
                                    b.postOwnerComment.setSelection(b.postOwnerComment.text.length)
                                    b.postOwnerComment.requestFocus()
                                    true
                                }
                                R.id.sharePost -> {
                                    val context = holder.itemView.context
                                    val postLink = "https://github.com/eraykstrl?tab=repositories"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT,"Paylaşmak istediğiniz gönderi")
                                        putExtra(Intent.EXTRA_TEXT, postLink)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent,"Gönderiyi paylaş"))
                                    true
                                }
                                R.id.savePost -> {
                                    currentuser?.uid?.let { userId ->
                                        viewModel.savePost(post, userId)
                                    }
                                    true
                                }
                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
                                    true
                                }
                                R.id.reportPostItem -> true
                                else -> false
                            }
                        }
                    }

                    is PostAdapter.PostHolder.FileHolder -> {
                        val b = holder.binding
                        popup.setOnMenuItemClickListener { item ->
                            when(item.itemId) {
                                R.id.modifyPost -> {
                                    b.savePostModify.visibility = View.VISIBLE
                                    b.postOwnerComment.isEnabled = true
                                    b.postOwnerComment.setSelection(b.postOwnerComment.text.length)
                                    b.postOwnerComment.requestFocus()
                                    true
                                }
                                R.id.sharePost -> {
                                    val context = holder.itemView.context
                                    val postLink = "https://github.com/eraykstrl?tab=repositories"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT,"Paylaşmak istediğiniz gönderi")
                                        putExtra(Intent.EXTRA_TEXT, postLink)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent,"Gönderiyi paylaş"))
                                    true
                                }
                                R.id.savePost -> {
                                    currentuser?.uid?.let { userId ->
                                        viewModel.savePost(post, userId)
                                    }
                                    true
                                }
                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
                                    true
                                }
                                R.id.reportPostItem -> true
                                else -> false
                            }
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
                            viewModel.deleteComment(comment)

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
            },
            clickPost = {

            },
            clickUsername = {
                    userId ->
                val action = DiscoverFragmentDirections.actionDiscoverFragment2ToProfileFragment(userId)
                updateUI(action)
            },
            fileClicked = {
                    fileName,fileUrl ->
                val localFile = File(requireContext().cacheDir,fileName)
                val storageRef = storage.getReferenceFromUrl(fileUrl)
                storageRef.getFile(localFile).addOnSuccessListener {
                    val uri = FileProvider.getUriForFile(requireContext(),"${requireContext().packageName}.fileProvider",localFile)

                    val mimeType = requireContext().contentResolver.getType(uri)
                        ?: MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(localFile.extension.lowercase())
                        ?: "application/octet-stream"

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri,mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    requireContext().startActivity(intent)
                }

            }

        )

        binding.discoverRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.discoverRecyclerView.adapter = postAdapter

        searchAdapter = SearchAdapter(searchList)
        binding.searchingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchingRecyclerView.adapter = searchAdapter

    }

    private fun observerLiveData()
    {
        viewModel.postLiveData.observe(viewLifecycleOwner) {
            post ->
            postAdapter?.updateAdapter(post)

        }

        viewModel.updatedPostLiveData.observe(viewLifecycleOwner) {
                it ->
            postAdapter?.updatePost(it)
        }

        viewModel.commentLiveData.observe(viewLifecycleOwner) {
                (postId,comment) ->
            val lastComment = comment.toMutableList()
            postAdapter?.showComments(postId,lastComment)
        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
                error ->
            if(error != null)
            {
                println(error)
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Bir hata oluştu")
                alert.setMessage("Bir hata oluştu lütfen tekrar deneyiniz ${error}")
                alert.setPositiveButton("Tamam") {
                        dialog,which->
                    dialog.dismiss()
                }
                alert.show()
            }
        }

        viewModel.searchLiveData.observe(viewLifecycleOwner) {
            result ->
            searchAdapter?.updateAdapter(result)
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