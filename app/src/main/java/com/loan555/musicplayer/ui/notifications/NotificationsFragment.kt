package com.loan555.musicplayer.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.*
import com.loan555.musicplayer.databinding.FragmentNotificationsBinding
import com.loan555.musicplayer.model.*
import com.loan555.musicplayer.service.*
import java.lang.Exception

class NotificationsFragment : Fragment(),
    com.loan555.musicplayer.ui.notifications.ListSongAdapter.OnItemClickListener {

    private lateinit var notificationsViewModel: AppViewModel
    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
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

        binding.recyclerSongsRelate.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        notificationsViewModel.mListSongRelateLiveData.observe(viewLifecycleOwner, Observer {
            // gán binding cho textView để nó theo dõi biến _text
            binding.recyclerSongsRelate.adapter = ListSongAdapter(this.requireContext(), it, this)
        })
        notificationsViewModel.isPlaying.observe(viewLifecycleOwner, {
            if (it)
                binding.play.setBackgroundResource(R.drawable.ic_pause)
            else binding.play.setBackgroundResource(R.drawable.ic_play)
        })
        notificationsViewModel.statePlay.observe(viewLifecycleOwner, {
            when (it) {
                //0 la tuan tu roi ket thuc
                //1 la lap lai list
                //2 la phat ngau nhien
                //3 lap lai 1 bai
                0 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_24)
                }
                1 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_24_color)
                }
                2 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_shuffle_24)
                }
                3 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24)
                }
            }
        })
        notificationsViewModel.title.observe(viewLifecycleOwner, {
            binding.nameSong.text = it
        })
        notificationsViewModel.artist.observe(viewLifecycleOwner, {
            binding.nameSinger.text = it
        })
//        notificationsViewModel.seekbarMax.observe(viewLifecycleOwner, {
//            binding.seekBar.max = it
//        })
//        notificationsViewModel.seekbarPos.observe(viewLifecycleOwner, {
//            binding.seekBar.progress = it
//        })
        notificationsViewModel.isStop.observe(viewLifecycleOwner, {
            if (!it)
                binding.playing.visibility = View.VISIBLE
            else binding.playing.visibility = View.GONE
        })
        binding.loop.setOnClickListener {
            var newState = notificationsViewModel.statePlay.value
            if (newState != null)
                notificationsViewModel.setStatePlay(newState + 1)
        }
        binding.play.setOnClickListener {
            notificationsViewModel.sentActionMusic(ACTION_PLAY_PAUSE)
            notificationsViewModel.sentActionMusic(0)
        }
        binding.skipNextPlay.setOnClickListener {
            notificationsViewModel.sentActionMusic(ACTION_NEXT)
            notificationsViewModel.sentActionMusic(0)
        }
        binding.skipBackPlay.setOnClickListener {
            notificationsViewModel.sentActionMusic(ACTION_BACK)
            notificationsViewModel.sentActionMusic(0)
        }
        binding.loadRelated.setOnClickListener {
            notificationsViewModel.loadClick()
        }
        Log.d(MY_TAG, "bind viewModel SearchFragment")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        notificationsViewModel.playSong(position, PLAYLIST_RELATED)
    }

    override fun onItemLongClick(v: View?, item: SongCustom, position: Int) {
        val popupMenu = PopupMenu(this.requireContext(), v)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_like -> {
                    Log.d(MY_TAG, "thêm vào bài hát yêu thích: $item")
                    notificationsViewModel.setOptionClick(R.id.popup_like,item)
                }
                R.id.popup_download -> {
                    Log.d(MY_TAG, "tải về: $item")
                    notificationsViewModel.sentDownLoad(item)
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