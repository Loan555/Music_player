package com.loan555.musicplayer.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.PLAYLIST_CHART
import com.loan555.musicplayer.R
import com.loan555.musicplayer.databinding.FragmentDashboardBinding
import com.loan555.musicplayer.model.*
import java.lang.Exception

class DashboardFragment : Fragment(), ListChartAdapter.OnItemClickListener {

    private lateinit var songAdapter: ListChartAdapter
    private lateinit var dashboardViewModel: AppViewModel
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MY_TAG, "DashboardFragment onCreateView")
        dashboardViewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerSongsChart
        recyclerView.layoutManager =
            LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)
        dashboardViewModel.mListSongChartLiveData.observe(viewLifecycleOwner, Observer {
            songAdapter = ListChartAdapter(this.requireContext(), it, this)
            recyclerView.adapter = songAdapter
            Log.d(MY_TAG, "songs chart = $it")
        })
        dashboardViewModel.chartLoading.observe(viewLifecycleOwner, {
            if (it) binding.progressChart.visibility = View.VISIBLE
            else binding.progressChart.visibility = View.GONE
        })
        binding.swipeRefresh.setOnRefreshListener {
            dashboardViewModel.getLoading(1)
            binding.swipeRefresh.isRefreshing = false
            dashboardViewModel.getLoading(-1)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        dashboardViewModel.playSong(position, PLAYLIST_CHART)
    }

    override fun onItemLongClick(v: View?, item: SongCustom, position: Int) {
        val popupMenu = PopupMenu(this.requireContext(), v)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_like -> {
                    Log.d(MY_TAG, "thêm vào bài hát yêu thích: $item")
                    dashboardViewModel.setOptionClick(R.id.popup_like,item)
                }
                R.id.popup_download -> {
                    Log.d(MY_TAG, "tải về: $item")
                    dashboardViewModel.sentDownLoad(item)
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