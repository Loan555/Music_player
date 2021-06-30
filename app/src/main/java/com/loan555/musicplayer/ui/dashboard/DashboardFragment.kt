package com.loan555.musicplayer.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.databinding.FragmentDashboardBinding
import com.loan555.musicplayer.model.AppModel
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.DataChartResult
import com.loan555.musicplayer.model.SongCustom
import com.loan555.musicplayer.ui.home.ListSongAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class DashboardFragment : Fragment(),ListSongAdapter.OnItemClickListener {

    private lateinit var songAdapter: ListSongAdapter
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
            ViewModelProviders.of(this).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerSongsChart
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext(),LinearLayoutManager.VERTICAL,false)
        dashboardViewModel.mListSongChartLiveData.observe(viewLifecycleOwner,{
            songAdapter = ListSongAdapter(it, this)
            recyclerView.adapter = songAdapter
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        Log.e(MY_TAG,"item chart click")
    }
}