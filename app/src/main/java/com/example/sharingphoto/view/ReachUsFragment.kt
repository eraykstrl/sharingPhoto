package com.example.sharingphoto.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.sharingphoto.R
import com.example.sharingphoto.databinding.FragmentReachUsBinding
import com.example.sharingphoto.viewmodel.ReachUsModelView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ReachUsFragment : Fragment() {

    private var _binding : FragmentReachUsBinding ?= null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel : ReachUsModelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentReachUsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ReachUsModelView::class.java]

        adapter()
        observerLiveData()

        val currentUser = auth.currentUser

        if(currentUser != null)
        {
            binding.sendButton.setOnClickListener {
                getProblems(currentUser)
            }
        }

        else
        {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Kullanıcı Hatası")
            alert.setMessage("Tekrar giriş yapmanız gerekiyor kullanıcı bilgisi alınamıyor")
            alert.setPositiveButton("Tamam") {
                dialog,which->
                val action = ReachUsFragmentDirections.actionReachUsFragmentToSignInFragment()
                updateUI(action)
            }
        }


    }


    private fun updateUI(action : NavDirections)
    {
        findNavController().navigate(action)
    }

    private fun adapter()
    {

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.contact_reasons,
            R.layout.array_row
        )

        binding.topicSpinner.adapter = adapter
    }


    private fun getProblems(user : FirebaseUser)
    {
        val spinner = binding.topicSpinner

        val selectedItem = spinner.selectedItem.toString()

        val otherProblems = binding.extraProblemEditText.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveProblems(user,selectedItem,otherProblems)
        }

    }

    private fun observerLiveData()
    {
        viewModel.problemsLiveData.observe(viewLifecycleOwner) {
            result ->
            if(result == 1)
            {
                Snackbar.make(requireView(),"Hata bildirimleri için teşekkür ederiz ",Snackbar.LENGTH_LONG)
                    .show()
            }
            else
            {
                Snackbar.make(requireView(),result.toString(),Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}