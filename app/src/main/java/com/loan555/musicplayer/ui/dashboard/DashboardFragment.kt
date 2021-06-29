package com.loan555.musicplayer.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.MainActivity
import com.loan555.musicplayer.databinding.FragmentDashboardBinding
import com.loan555.musicplayer.model.AppModel
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.DataChartResult
import com.loan555.musicplayer.service.ApiChartService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class DashboardFragment : Fragment() {

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
            ViewModelProvider(this).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        getCurrentChartData()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentChartData() {
        Log.d(MY_TAG, "getCurrentChartData")
        val call = AppModel.serviceApiGetChart.getCurrentData(
            AppModel.songId,
            AppModel.videoId,
            AppModel.albumId,
            AppModel.chart,
            AppModel.time
        )
        call.enqueue(object : Callback<DataChartResult> {
            override fun onResponse(
                call: Call<DataChartResult>,
                response: Response<DataChartResult>
            ) {
                if (response.code() == 200) {
                    val dataResponse = response.body()!!
                    dashboardViewModel._text.apply {
                        value = dataResponse.data.toString()
                    }
                    Log.d(MY_TAG, "loadData success")
                } else Log.e("aaa", "response.code() = ${response.code()}")
            }

            override fun onFailure(call: Call<DataChartResult>, t: Throwable) {
                Log.e(MY_TAG, "error getCurrentChartData ${t.message}")
            }
        })
    }
}