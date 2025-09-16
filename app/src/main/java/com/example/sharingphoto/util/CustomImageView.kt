package com.example.sharingphoto.util

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CustomImageView @JvmOverloads constructor(
    context : Context,
    attrs : AttributeSet ?= null,
    defStyleAttrs : Int =0

)  : AppCompatImageView(context,attrs,defStyleAttrs){


    override fun performClick(): Boolean {
        return super.performClick()
    }
}