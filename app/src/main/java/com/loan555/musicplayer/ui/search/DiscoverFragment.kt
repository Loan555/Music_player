package com.loan555.musicplayer.ui.search

import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.*
import com.loan555.musicplayer.databinding.FragmentDiscoverBinding
import com.loan555.musicplayer.databinding.FragmentNotificationsBinding
import com.loan555.musicplayer.model.*
import com.loan555.musicplayer.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class DiscoverFragment : Fragment(),
    com.loan555.musicplayer.ui.notifications.ListSongAdapter.OnItemClickListener {

    private lateinit var viewModel: AppViewModel
    private var _binding: FragmentDiscoverBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val search = menu?.findItem(R.id.menu_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("aaa", "search list submit")
                if (query != null) {
                    viewModel.getLoad(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("aaa", "onQueryTextChange")
                return false
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView: RecyclerView = binding.recyclerSongs

        recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        viewModel.mListSongSearchLiveData.observe(viewLifecycleOwner, Observer {
            // gán binding cho textView để nó theo dõi biến _text
            recyclerView.adapter = com.loan555.musicplayer.ui.notifications.ListSongAdapter(
                this.requireContext(),
                it,
                this
            )
        })
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getLoading(2)
            binding.swipeRefresh.isRefreshing = false
            viewModel.getLoading(-2)
        }
        Log.d(MY_TAG, "bind viewModel SearchFragment")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        viewModel.playSong(position, PLAYLIST_SEARCH)
    }

    override fun onItemLongClick(v: View?, item: SongCustom, position: Int) {
        val popupMenu = PopupMenu(this.requireContext(), v)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_like -> {
                    Log.d(MY_TAG, "thêm vào bài hát yêu thích: $item")
                    viewModel.setOptionClick(R.id.popup_like, item)
                }
                R.id.popup_download -> {
                    Log.d(MY_TAG, "tải về: $item")
                    viewModel.sentDownLoad(item)
                }
                R.id.popup_add_playlist -> {
                    Log.d(MY_TAG, "them vao danh sach phat: $item")
                }
            }
            true
        }
        popupMenu.show()
    }
}