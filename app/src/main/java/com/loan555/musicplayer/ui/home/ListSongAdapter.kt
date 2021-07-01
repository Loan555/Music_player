package com.loan555.musicplayer.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.R
import com.loan555.musicplayer.model.SongCustom

class ListSongAdapter(
    private val listSong: ArrayList<SongCustom>,
    val listener: OnItemClickListener
) :
    RecyclerView.Adapter<ListSongAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        val name: TextView = itemView.findViewById(R.id.song_name)
        val artist: TextView = itemView.findViewById(R.id.artists_names)
        val img: ImageView = itemView.findViewById(R.id.img_song)
        val time: TextView = itemView.findViewById(R.id.duration)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClick(v, listSong[layoutPosition], layoutPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            listener.onLongClick(v, listSong[layoutPosition], layoutPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.song_adapter, parent, false)
    )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = listSong[position].title
        holder.artist.text = listSong[position].artists
        if (listSong[position].bitmap != null)
            holder.img.setImageBitmap(listSong[position].bitmap)
        holder.time.text = listSong[position].timeToString()
    }

    override fun getItemCount(): Int = listSong.size

    interface OnItemClickListener {
        fun onItemClick(v: View?, item: SongCustom, position: Int)
        fun onLongClick(v: View?, item: SongCustom, position: Int)
    }
}