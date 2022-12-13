package com.bqliang.leavesheet.utils

import android.media.AudioAttributes
import android.media.SoundPool
import com.bqliang.leavesheet.MyApp
import com.bqliang.leavesheet.R

object SoundPoolUtil {
    private val audioAttributes by lazy {
        AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .build()
    }

    private var soundPool: SoundPool? = null

    fun beep() {
        if (soundPool == null) {
            soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build()
        }

        val soundId = soundPool?.load(MyApp.context, R.raw.beep, 1)
        soundPool?.setOnLoadCompleteListener { _, _, _ ->
            soundPool?.play(
                /* soundID = */ soundId!!,
                /* leftVolume = */ 1f,
                /* rightVolume = */ 1f,
                /* priority = */ 0,
                /* loop = */ 0,
                /* rate = */ 1f
            )
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}