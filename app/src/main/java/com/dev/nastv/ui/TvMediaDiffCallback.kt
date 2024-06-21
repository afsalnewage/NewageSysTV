package com.dev.nastv.ui

import androidx.recyclerview.widget.DiffUtil
import com.dev.nastv.model.TvMedia

class TvMediaDiffCallback : DiffUtil.ItemCallback<TvMedia>() {
    override fun areItemsTheSame(oldItem: TvMedia, newItem: TvMedia): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: TvMedia, newItem: TvMedia): Boolean {
        return oldItem == newItem
    }
}