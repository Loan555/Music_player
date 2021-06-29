package com.loan555.musicplayer.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.R
import com.loan555.musicplayer.model.SongCustom

class ListSongAdapter(private val listSong: ArrayList<SongCustom>) :
    RecyclerView.Adapter<ListSongAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.song_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.song_adapter, parent, false)
    )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = listSong[position].title
    }

    override fun getItemCount(): Int = listSong.size
}