package com.loan555.musicplayer.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.*
import com.loan555.musicplayer.databinding.FragmentNotificationsBinding
import com.loan555.musicplayer.model.*
import com.loan555.musicplayer.model.AppModel.Companion.type
import com.loan555.musicplayer.ui.home.ListSongAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class NotificationsFragment : Fragment(), ListSongAdapter.OnItemClickListener {

    private lateinit var notificationsViewModel: AppViewModel
    private var _binding: FragmentNotificationsBinding? = null

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
                    notificationsViewModel.getLoad(query)
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
        notificationsViewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView: RecyclerView = binding.recyclerSongs

        recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        notificationsViewModel.mListSongSearchLiveData.observe(viewLifecycleOwner, Observer {
            // gán binding cho textView để nó theo dõi biến _text
            recyclerView.adapter = ListSongAdapter(it,this)
        })
        Log.d(MY_TAG, "bind viewModel SearchFragment")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        notificationsViewModel.playSong(position, PLAYLIST_SEARCH)
    }
}