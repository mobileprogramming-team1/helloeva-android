package com.helloeva.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Records the user's answer to an m4a (AAC) file in the cache folder.
 * The API accepts m4a/aac/wav, at most 2 minutes and 10 MB;
 * 64 kbps × 2 min ≈ 1 MB, so size is never a problem.
 * Call [start] only after RECORD_AUDIO is granted.
 */
class AnswerRecorder(context: Context) {

    private val appContext = context.applicationContext
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    val isRecording: Boolean get() = recorder != null

    /** Throws if the mic can't be opened (e.g. another app is using it). */
    fun start() {
        check(recorder == null) { "Already recording" }
        val out = File(appContext.cacheDir, "answer-${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(64_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(out.absolutePath)
            r.prepare()
            r.start()
        } catch (e: Exception) {
            r.release()
            out.delete()
            throw e
        }
        recorder = r
        file = out
    }

    /**
     * Stops and returns the file, or null if the recording failed
     * (stopped before any audio was written, or the audio track hit an error).
     */
    fun stop(): File? {
        val r = recorder ?: return null
        val out = file
        recorder = null
        file = null
        return try {
            r.stop()
            out
        } catch (e: RuntimeException) {
            // MediaRecorder throws when no audio was written or the track failed mid-way;
            // the file can't be trusted then.
            out?.delete()
            null
        } finally {
            r.release()
        }
    }

    /** Stops and throws the recording away. */
    fun cancel() {
        stop()?.delete()
    }
}
