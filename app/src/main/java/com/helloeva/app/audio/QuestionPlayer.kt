package com.helloeva.app.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Plays Eva's question audio (TTS file from the server).
 * Use from the main thread only (ExoPlayer requirement); viewModelScope is fine.
 */
class QuestionPlayer(context: Context) {

    private val player = ExoPlayer.Builder(context.applicationContext).build()

    /**
     * Plays [url] from the start and returns when it ends.
     * [onProgress] gets 0..1 about every 100 ms.
     * Throws [PlaybackException] if the file can't be played.
     * Cancelling the coroutine stops playback.
     */
    suspend fun play(url: String, onProgress: (Float) -> Unit) {
        val ended = CompletableDeferred<Unit>()
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) ended.complete(Unit)
            }

            override fun onPlayerError(error: PlaybackException) {
                ended.completeExceptionally(error)
            }
        }
        player.addListener(listener)
        try {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
            coroutineScope {
                val ticker = launch {
                    while (true) {
                        val duration = player.duration
                        if (duration > 0) onProgress((player.currentPosition.toFloat() / duration).coerceIn(0f, 1f))
                        delay(100)
                    }
                }
                try {
                    ended.await()
                } finally {
                    ticker.cancel()
                }
            }
            onProgress(1f)
        } finally {
            player.removeListener(listener)
            player.stop()
        }
    }

    /** Pause while the app is in the background. */
    fun pause() = player.pause()

    fun resume() {
        if (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) player.play()
    }

    fun release() = player.release()
}
