package com.dev.nastv.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.nastv.model.TvMedia

//(val fa:FragmentActivity, val mediaList:ArrayList<TvMedia>): FragmentStateAdapter(fa)

class UiPagesAdapter(val fa:FragmentActivity, var mediaList:ArrayList<TvMedia>): FragmentStateAdapter(fa) {

    fun updateData(item: List<TvMedia>) {
    Log.d("TTTT","adding item${item}")
      //  mediaList.addAll(item)
      //  notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return  mediaList.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun createFragment(position: Int): Fragment {

        val mData=mediaList[position]
//        return when (mediaList[position].mediaType) {
//            Type.Video -> {
//                VideoFragment.newInstance(mData)
//            }
//            Type.Image,Type.NewJoinee,Type.Anniversary,Type.Birthday -> {
//                ImageFragment.newInstance(mData)
//            }

          return  when (mediaList[position].event_type) {
                "Video" -> {
                    VideoFragment.newInstance(mData,mediaList.size)

                }
                "Image" -> {
                    ImageFragment.newInstance(mData)
                }
                "Anniversary" -> {
                    ImageFragment.newInstance(mData)

                }
                "Birthday" -> {
                    ImageFragment.newInstance(mData)
                }
                "New Joinee" -> {
                    ImageFragment.newInstance(mData)
                }

              else -> {Fragment()}
          }
    }
}