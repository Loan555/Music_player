package com.loan555.musicplayer.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.databinding.FragmentHomeBinding
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.SongCustom
import java.lang.Exception

class HomeFragment : Fragment() {

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
            ViewModelProvider(this).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // khai báo view binding
        val textView: TextView = binding.textView1
        val recyclerView: RecyclerView = binding.recyclerSongs

        recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        homeViewModel.mListSongLiveData.observe(viewLifecycleOwner, Observer {
            // gán binding cho textView để nó theo dõi biến _text
            songAdapter = ListSongAdapter(it)
            recyclerView.adapter = songAdapter
        })
        Log.d(MY_TAG,"bind viewModel HomeFragment")

        textView.setOnClickListener {
            Toast.makeText(this.context, " add ", Toast.LENGTH_SHORT).show()
            val song = SongCustom(
                "id",
                "tên bài hát̃",
                "tên ca sĩ", 100, 12, "tên hiển thị", "tên albums", true, "link ủi"
            )
            homeViewModel.addData(song)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}