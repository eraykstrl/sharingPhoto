package com.example.sharingphoto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharingphoto.R
import com.example.sharingphoto.databinding.FragmentAboutUsBinding


class AboutUsFragment : Fragment() {

    private var _binding : FragmentAboutUsBinding ?= null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentAboutUsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = readTextFile(R.raw.about_me)
        binding.aboutMeTextView.text = result
    }


    private fun readTextFile(resourceId : Int) : String
    {
        return resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}