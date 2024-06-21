package com.dev.nastv.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.nastv.model.TvMedia
import com.smb.app.addsapp.model.MediaItemData
import com.smb.app.addsapp.model.Type
//(val fa:FragmentActivity, val mediaList:ArrayList<TvMedia>): FragmentStateAdapter(fa)

class UiPagesAdapter(val fa:FragmentActivity, val mediaList:ArrayList<TvMedia>): FragmentStateAdapter(fa) {
//    override fun getCount()=mediaList.size
//
//    override fun getItem(position: Int): Fragment {
//
//
//          val mData=mediaList[position]
//        return when (mediaList[position].mediaType) {
//            Type.Video -> {
//                VideoFragment.newInstance(mData)
//            }
//            Type.Image -> {
//                ImageFragment.newInstance(mData)
//            }
//        }
//
//
//
//    }

    override fun getItemCount(): Int {
        return  mediaList.size
    }

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
                    VideoFragment.newInstance(mData)

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