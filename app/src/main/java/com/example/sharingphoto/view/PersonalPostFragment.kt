package com.example.sharingphoto.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.appcompat.widget.PopupMenu
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.PostAdapter
import com.example.sharingphoto.databinding.FragmentPersonalPostBinding
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.viewmodel.PersonalPostViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class PersonalPostFragment : Fragment() {

    private var _binding : FragmentPersonalPostBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private var postAdapter : PostAdapter ?=null
    private val postList : ArrayList<Post> = arrayListOf()
    private lateinit var storage : FirebaseStorage

    private lateinit var viewModel : PersonalPostViewModel
    private var currentUser : FirebaseUser ?= null
    private var currentUserId : String ?= null
    private var info : String ?= null
    private var profileId : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage
        auth = Firebase.auth
        viewModel = ViewModelProvider(this)[PersonalPostViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentPersonalPostBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentUser = auth.currentUser
        if(currentUser != null)
        {
            currentUserId = currentUser?.uid
            arguments?.let {
                info = PersonalPostFragmentArgs.fromBundle(it).postId
                profileId = PersonalPostFragmentArgs.fromBundle(it).profileId
                postAdapter()
                observerLiveData()

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getPostByUser(profileId!!)
                }


                binding.backIcon.setOnClickListener {
                    val action = PersonalPostFragmentDirections.actionPersonalPostFragmentToProfileFragment(profileId!!)
                    findNavController().navigate(action)
                }
            }

        }

    }


    private fun postAdapter()
    {
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
                if(comment.user_id == currentUserId)
                {
                    comment.isCommentOwner = true
                }

            },
            postUserId = {
                post->
                val postOwnerId = post.user_id
                if(postOwnerId == currentUserId)
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
                viewModel.updateLike(post,info,currentUserId!!)

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

                when(holder)
                {
                    is PostAdapter.PostHolder.ImageHolder -> {
                        val binding = holder.binding
                        popup.setOnMenuItemClickListener {
                                item ->
                            when(item.itemId)
                            {
                                R.id.modifyPost -> {
                                    binding.savePostModify.visibility = View.VISIBLE
                                    binding.postOwnerComment.isEnabled = true
                                    binding.postOwnerComment.setSelection(holder.binding.postOwnerComment.text.length)
                                    binding.postOwnerComment.requestFocus()
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
                                    viewModel.savePost(post,currentUserId!!)

                                    true
                                }

                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
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
                    }
                    is PostAdapter.PostHolder.VideoHolder -> {
                        val binding = holder.binding
                        popup.setOnMenuItemClickListener {
                                item ->
                            when(item.itemId)
                            {
                                R.id.modifyPost -> {
                                    binding.savePostModify.visibility = View.VISIBLE
                                    binding.postOwnerComment.isEnabled = true
                                    binding.postOwnerComment.setSelection(holder.binding.postOwnerComment.text.length)
                                    binding.postOwnerComment.requestFocus()
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
                                    viewModel.savePost(post,currentUserId!!)

                                    true
                                }

                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
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
                    }
                    is PostAdapter.PostHolder.TextHolder -> {
                        val binding = holder.binding
                        popup.setOnMenuItemClickListener {
                                item ->
                            when(item.itemId)
                            {
                                R.id.modifyPost -> {
                                    binding.savePostModify.visibility = View.VISIBLE
                                    binding.postOwnerComment.isEnabled = true
                                    binding.postOwnerComment.setSelection(holder.binding.postOwnerComment.text.length)
                                    binding.postOwnerComment.requestFocus()
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
                                    viewModel.savePost(post,currentUserId!!)

                                    true
                                }

                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
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
                    }
                    is PostAdapter.PostHolder.FileHolder-> {
                        val binding = holder.binding
                        popup.setOnMenuItemClickListener {
                                item ->
                            when(item.itemId)
                            {
                                R.id.modifyPost -> {
                                    binding.savePostModify.visibility = View.VISIBLE
                                    binding.postOwnerComment.isEnabled = true
                                    binding.postOwnerComment.setSelection(holder.binding.postOwnerComment.text.length)
                                    binding.postOwnerComment.requestFocus()
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
                                    viewModel.savePost(post,currentUserId!!)

                                    true
                                }

                                R.id.deletePostItem -> {
                                    viewModel.deletePost(post)
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
                post ->
            },
            clickUsername = {
                userId ->
                val action = PersonalPostFragmentDirections.actionPersonalPostFragmentToProfileFragment(userId)
                updateUI(action)
            },
            fileClicked = {
                    fileName,url ->
                    val storageRef = storage.getReferenceFromUrl(url)
                    val localFile = File(requireContext().cacheDir,fileName)

                    storageRef.getFile(localFile).addOnSuccessListener {
                        val uri = FileProvider.getUriForFile(requireContext(),"${requireContext().packageName}.fileProvider",localFile)

                        val mime = MimeTypeMap.getSingleton()
                        val ext = MimeTypeMap.getFileExtensionFromUrl(localFile.name)
                        val type = mime.getMimeTypeFromExtension(ext)

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri,type)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        requireContext().startActivity(intent)
                    }

            }

        )


    }

    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun observerLiveData()
    {
        viewModel.personalProfileLiveData.observe(viewLifecycleOwner) {
            posts ->
            postList.clear()
            postList.addAll(posts)
            postAdapter?.updateAdapter(posts)

            binding.personalRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.personalRecyclerView.adapter = postAdapter

            var startPosition = 0
            if(!info.isNullOrEmpty())
            {
                val index = postList.indexOfFirst {it.postId == info  }
                if(index != -1)
                {
                    startPosition = index
                }
            }

            binding.personalRecyclerView.scrollToPosition(startPosition)
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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}