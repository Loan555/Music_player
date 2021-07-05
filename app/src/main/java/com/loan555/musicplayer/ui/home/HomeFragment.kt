package com.loan555.musicplayer.ui.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.PLAYLIST_STORAGE
import com.loan555.musicplayer.R
import com.loan555.musicplayer.databinding.FragmentHomeBinding
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.SongCustom
import java.lang.Exception

class HomeFragment : Fragment(), ListSongAdapter.OnItemClickListener {

    private lateinit var songAdapter: ListSongAdapter
    private lateinit var homeViewModel: AppViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // khai báo view binding
        val recyclerView: RecyclerView = binding.recyclerSongs

        recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        homeViewModel.mListSongLiveData.observe(viewLifecycleOwner, Observer {
            // gán binding cho textView để nó theo dõi biến _text
            songAdapter = ListSongAdapter(it, this)
            recyclerView.adapter = songAdapter
        })
        homeViewModel.homeLoading.observe(viewLifecycleOwner, {
            if (it) binding.progressHome.visibility = View.VISIBLE
            else binding.progressHome.visibility = View.GONE
        })
        binding.swipeRefresh.setOnRefreshListener {
            Toast.makeText(this.requireContext(), "load", Toast.LENGTH_SHORT).show()
            homeViewModel.getLoading(0)
            binding.swipeRefresh.isRefreshing = false
            homeViewModel.getLoading(-1)
        }
        Log.d(MY_TAG, "bind viewModel HomeFragment")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        homeViewModel.playSong(position, PLAYLIST_STORAGE)
    }

    override fun onLongClick(v: View?, item: SongCustom, position: Int) {
        val popupMenu = PopupMenu(this.requireContext(), v)
        popupMenu.inflate(R.menu.local_popup)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_like -> {
                    Log.d(MY_TAG, "thêm vào bài hát yêu thích: $item")
                    homeViewModel.setOptionClick(R.id.popup_like,item)
                }
                R.id.popup_add_playlist -> {
                    Log.d(MY_TAG, "them vao danh sach phat: $item")
                }
            }
            true
        }
        popupMenu.show()
    }

    /**
     * Data Storage
     */
}