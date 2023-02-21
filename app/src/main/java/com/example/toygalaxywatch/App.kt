package com.example.toygalaxywatch

import android.app.Application
import android.content.Intent
import android.os.Build
import com.example.toygalaxywatch.service.HfRtmService
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class App : Application() {

    private var mSocket: Socket? = null

    override fun onCreate() {
        super.onCreate()


        // RTM 소캣을 생성해둔다.
        // LifeCycle 상 앱 사용 전반에 사용해야 하므로 앱생성시 제일 먼저 생성햔다.
        try {
            mSocket = IO.socket("http://msg.hellobell.net:8080/hb_staff")

            // 안드로이드 O 이후 백그라운드 서비스 제한이 되면서
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, HfRtmService::class.java))
            } else {
                startService(Intent(this, HfRtmService::class.java))
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun getSocket() = mSocket


    companion object {

        private var INSTANCE: App? = null

        fun getInstance(): App = INSTANCE ?: App().apply {
            INSTANCE = this
        }
    }

}