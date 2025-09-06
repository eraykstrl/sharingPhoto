package com.example.sharingphoto.view

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
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
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PersonalPostFragment : Fragment() {

    private var _binding : FragmentPersonalPostBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private var postAdapter : PostAdapter ?=null
    private val postList : ArrayList<Post> = arrayListOf()

    private lateinit var viewModel : PersonalPostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


        arguments?.let {
            val info = PersonalPostFragmentArgs.fromBundle(it).userId
            postAdapter(info)
            observerLiveData()

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getPostByUser(info)
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
                            val userId = info
                            viewModel.savePost(post,userId)

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
            }

        )

        binding.personalRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.personalRecyclerView.adapter = postAdapter
    }

    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun observerLiveData()
    {
        viewModel.personalProfileLiveData.observe(viewLifecycleOwner) {
            postList ->
            postAdapter?.updateAdapter(postList)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}