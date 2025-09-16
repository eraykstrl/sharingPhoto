package com.example.sharingphoto.util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

class VideoView {


    fun getVideoThumbnail(videoUrl : String) : Bitmap? {
        try
        {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap())
            val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            retriever.release()
            return bitmap
        }

        catch (e : Exception)
        {

            return null
        }
    }
}