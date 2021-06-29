package com.loan555.musicplayer.service

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.model.*
import java.io.IOException
import java.lang.Exception

class MusicControllerService : Service() {
    private val binder = MusicControllerBinder()
    private var player: MediaPlayer? = null
    private var songPos = 0
    private val songs = ArrayList<SongCustom>()
    var songsStorage = ArrayList<SongCustom>()

    private lateinit var mainViewModel: AppViewModel

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class MusicControllerBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MusicControllerService = this@MusicControllerService
    }

    override fun onCreate() {
        Log.d("aaa", "service onCreate")
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("aaa", "service onBind")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    /**
     * Data Storage
     */

    private fun checkPermissionStorage(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    fun getListFromStorage(): Boolean {
        var resultOK = false
        if (!checkPermissionStorage()) {
            Toast.makeText(this, "Permission storage is deni", Toast.LENGTH_SHORT).show()
            return resultOK
        }
        val collection: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val protection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = null
        val selectionArgs = null
        val sortOder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = this.applicationContext.contentResolver.query(
            collection,
            protection,
            selection,
            selectionArgs,
            sortOder
        )
        val newSongs = ArrayList<SongCustom>()
        try {
            query?.use { cursor ->
                newSongs.clear()
                val options = BitmapFactory.Options()
                options.outHeight = 480
                options.outWidth = 640
                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                while (cursor.moveToNext()) {
                    // Get values of columns for a given audio.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val artists = cursor.getString(artistsColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)
                    val title = cursor.getString(titleColumn)
                    val albums = cursor.getString(albumsColumn)

                    //load content Uri
                    val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    // Load thumbnail of a specific media item.
                    var thumbnail: Bitmap? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val thumb = this.applicationContext.contentResolver.loadThumbnail(
                                contentUri, Size(640, 480), null
                            )
                            if (thumb != null)
                                thumbnail = thumb
                        } catch (e: IOException) {
                            Log.e(MY_TAG, "can't find bitmap: ${e.message}")
                        }
                    }
                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    val newSong = SongCustom(
                        id.toString(),
                        name,
                        artists,
                        duration,
                        size,
                        title,
                        albums,
                        true,
                        contentUri.toString()
                    )
                    newSongs.add(newSong)
                }
            }
        } catch (e: Exception) {
            Log.e(MY_TAG, "error getListFromStorage: ${e.message}")
        }
        if (newSongs.size != 0) {
            songsStorage = newSongs
            resultOK = true
        }
        return resultOK
    }

    /**     music controller */
    private fun playSong(linkUri: String) {
        Toast.makeText(this, "song is playing", Toast.LENGTH_LONG).show()
        player?.release()
        val uri: Uri = Uri.parse(linkUri)
        player = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(applicationContext, uri)
            prepare()
            start()
        }
    }

    fun playNext() {
        Toast.makeText(this, "next", Toast.LENGTH_SHORT).show()
        songPos++;
        if (songPos == songs.size) songPos = 0;
        this.playSong("api.mp3.zing.vn/api/streaming/audio/${songs[songPos].id}/320")
    }

    fun playPrev() {
        Toast.makeText(this, "back", Toast.LENGTH_SHORT).show()
        songPos--;
        if (songPos == 0) songPos = songs.size - 1;
        this.playSong("api.mp3.zing.vn/api/streaming/audio/ZWBIF86E/320")
    }

    fun getPos(): Int? {
        return player?.currentPosition
    }

    fun getDur(): Int? {
        return player?.duration
    }

    fun isPng(): Boolean? {
        return player?.isPlaying
    }

    fun pausePlayer() {
        player?.pause()
    }

    fun seek(position: Int) {
        player?.seekTo(position)
    }
}