package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingphoto.R
import com.example.sharingphoto.adapter.PostAdapter
import com.example.sharingphoto.databinding.FragmentFeedBinding
import com.example.sharingphoto.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var _binding : FragmentFeedBinding ?= null
    private val binding get() = _binding!!

    private lateinit var popUp : PopupMenu

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    val postList : ArrayList<Post> = arrayListOf()

    private var adapter : PostAdapter ?= null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingActionButton(it) }

        popUp = PopupMenu(requireContext(),binding.floatingActionButton)
        val inflater = popUp.menuInflater
        inflater.inflate(R.menu.my_pop_menu,popUp.menu)
        popUp.setOnMenuItemClickListener(this)

        getDatas()

        adapter = PostAdapter(postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter

    }

    private fun getDatas()
    {
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value,error ->
            if(error != null)
            {
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }

            else
            {
                if(value != null)
                {
                    if(!value.isEmpty)
                    {
                        val documents = value.documents
                        postList.clear()
                        for(document in documents)
                        {
                            val comment = document.get("comment") as String
                            val email = document.get("email") as String
                            val downloadUrl = document.get("downloadUrl") as String

                            val post = Post(comment, email, downloadUrl)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                }
            }

        }
    }

    fun floatingActionButton(view : View)
    {
        popUp.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.uploadingItem)
        {
            val action = FeedFragmentDirections.actionFeedFragmentToUploadFragment()
            findNavController().navigate(action)
        }
        else if(item?.itemId == R.id.logOutItem)
        {
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToUserFragment()
            findNavController().navigate(action)
        }

        return true
    }

}