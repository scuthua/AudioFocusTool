package com.flower.audiofocustool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat


class AudioFocusService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "AudioFocusService"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        // 打印一下，用来判断服务是否启动了。
        Log.d(TAG, "onCreate:")
        //        // 获取服务通知
        //        val notification = createForegroundNotification()
        //        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        //        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ret = super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand:$ret")
        val type = intent?.getStringExtra("type")
        // TODO: 2021/8/20 外面可能传错这个int数值，怎么告知有问题呢？能不能让adb里打印错误信息呢？
        // TODO: 2021/8/20 加个help指令，用来看当前支持哪些东西。不然我自己都不记得怎么用。
        val stream = intent?.getIntExtra("stream",AudioManager.STREAM_MUSIC)?:AudioManager.STREAM_MUSIC
        Log.d(TAG, "onStartCommand:type=$type stream=$stream")
        // todo 在这里添加更多的指令来用控制焦点
        when (type) {
            "request" -> {
                request(AudioManager.AUDIOFOCUS_GAIN,stream)
            }
            "abandon" -> {
                abandon(stream)
            }
        }
        return ret
    }

    // 暂时没用到，随便返回一个Binder
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind:")
        return EmptyBinder()
    }

    class EmptyBinder : Binder()

    private fun request(audioFocus: Int, stream: Int) {
        val audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setLegacyStreamType(stream)
                .build()
            audioManager.requestAudioFocus(
                AudioFocusRequest.Builder(audioFocus)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            )
        } else {
            audioManager.requestAudioFocus(this, stream, audioFocus)
        }
    }

    private fun abandon(stream: Int) {
        val audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setLegacyStreamType(stream)
                .build()
            val audioFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.d(TAG, "onAudioFocusChange:$focusChange")
    }

    private fun createForegroundNotification(): Notification? {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 唯一的通知通道的id.

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            val channelName = "AudioFocusTools Notification"
            //通道的重要程度
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(NOTIFICATION_ID.toString(), channelName, importance)
            notificationChannel.description = "is used to test audio focus"
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_ID.toString())
        //通知小图标
        builder.setSmallIcon(R.mipmap.ic_launcher)
        //通知标题
        builder.setContentTitle("AudioFocusTools")
        //通知内容
        builder.setContentText(null)
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis())
        //设定启动的内容
        builder.setContentIntent(null)

        //创建通知并返回
        return builder.build()
    }

}
