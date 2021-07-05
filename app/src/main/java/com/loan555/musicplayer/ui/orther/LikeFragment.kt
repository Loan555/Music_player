package com.loan555.musicplayer.ui.orther

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.PLAYLIST_Like
import com.loan555.musicplayer.PLAYLIST_SEARCH
import com.loan555.musicplayer.databinding.FragmentLikeBinding
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.SongCustom
import com.loan555.musicplayer.ui.notifications.ListSongAdapter
import java.lang.Exception

class LikeFragment : Fragment(),ListSongAdapter.OnItemClickListener {
    private lateinit var viewModel: AppViewModel
    private var _binding: FragmentLikeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")
        _binding = FragmentLikeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView: RecyclerView = binding.recyclerSongs

        recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        viewModel.mListSongLikeLiveData.observe(viewLifecycleOwner, {
            // gán binding cho textView để nó theo dõi biến _text
            recyclerView.adapter = com.loan555.musicplayer.ui.notifications.ListSongAdapter(
                this.requireContext(),
                it,
                this
            )
        })
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getLoading(4)
            binding.swipeRefresh.isRefreshing = false
            viewModel.getLoading(-2)
        }
        binding.button.setOnClickListener {
            viewModel.setBtnLikeClick()
        }

        // Inflate the layout for this fragment
        return root
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        viewModel.playSong(position, PLAYLIST_Like)
    }

    override fun onItemLongClick(v: View?, item: SongCustom, position: Int) {

    }

}