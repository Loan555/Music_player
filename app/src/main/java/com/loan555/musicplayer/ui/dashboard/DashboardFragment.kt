package com.loan555.musicplayer.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.PLAYLIST_CHART
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
            songAdapter = ListChartAdapter(it, this)
            recyclerView.adapter = songAdapter
            Log.d(MY_TAG, "songs chart = $it")
        })
        dashboardViewModel.tem.observe(viewLifecycleOwner, {
            binding.title.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        dashboardViewModel.playSong(position, PLAYLIST_CHART)
    }
}