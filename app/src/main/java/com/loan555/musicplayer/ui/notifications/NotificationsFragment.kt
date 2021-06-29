package com.loan555.musicplayer.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.R
import com.loan555.musicplayer.databinding.FragmentNotificationsBinding
import com.loan555.musicplayer.model.*
import com.loan555.musicplayer.model.AppModel.Companion.type
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class NotificationsFragment : Fragment() {

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
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("aaa", "search list submit")
                //                adapter.filter.filter(newText)
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
            ViewModelProvider(this).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text2.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        getCurrentSongData("kmJHTZHNCVaSmSuymyFHLH")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentSongData(keySong: String) {// get data with key
        var song: Song? = null
        val call = AppModel.serviceApiGetSong.getCurrentData(type, keySong)
        call.enqueue(object : Callback<DataSongResult> {
            override fun onResponse(
                call: Call<DataSongResult>,
                response: Response<DataSongResult>
            ) {
                if (response.code() == 200) {
                    val dataResponse = response.body()!!.data
                    //load du lieu
                    song = dataResponse
                    notificationsViewModel._text2.value = song.toString()
                } else Log.e("aaa", "response.code() = ${response.code()}")
            }

            override fun onFailure(call: Call<DataSongResult>, t: Throwable) {
                Log.e(MY_TAG, "error getCurrentSongData ${t.message}")
            }
        })
    }
}