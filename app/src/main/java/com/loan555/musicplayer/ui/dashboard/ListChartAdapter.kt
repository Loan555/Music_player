package com.loan555.musicplayer.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.R
import com.loan555.musicplayer.model.SongCustom

class ListChartAdapter(
    private val listSong: ArrayList<SongCustom>,
    val listener: OnItemClickListener
) :
    RecyclerView.Adapter<ListChartAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        val songPos: TextView = itemView.findViewById(R.id.position)
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
            listener.onItemLongClick(v, listSong[layoutPosition], layoutPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.chart_adapter, parent, false)
    )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.songPos.text = "${position + 1}"
        when (position) {
            0 ->
                holder.songPos.setTextColor(Color.parseColor("#FF6200EE"))
            1 ->
                holder.songPos.setTextColor(Color.parseColor("#FF03DAC5"))
            2 ->
                holder.songPos.setTextColor(Color.parseColor("#CF7B00"))
            else -> holder.songPos.setTextColor(Color.parseColor("#A5A19C"))
        }
        holder.name.text = listSong[position].title
        holder.artist.text = listSong[position].artists
        if (listSong[position].bitmap != null)
            holder.img.setImageBitmap(listSong[position].bitmap)
        holder.time.text = listSong[position].timeToString()
    }

    override fun getItemCount(): Int = listSong.size

    interface OnItemClickListener {
        fun onItemClick(v: View?, item: SongCustom, position: Int)
        fun onItemLongClick(v: View?, item: SongCustom, position: Int)
    }
}