package com.example.edugo_fe

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "${BuildConfig.KAKAO_APP_KEY}")
    }
}