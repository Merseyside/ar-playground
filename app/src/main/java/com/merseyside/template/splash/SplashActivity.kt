package com.merseyside.template.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.merseyside.archy.presentation.activity.BaseActivity
import com.merseyside.template.R
import com.merseyside.template.news.main.view.MainActivity
import com.merseyside.utils.HandlerCanceller
import com.merseyside.utils.delayedMainThread
import com.merseyside.utils.time.Seconds

class SplashActivity : BaseActivity() {

    private lateinit var canceller: HandlerCanceller

    override fun performInjection(bundle: Bundle?) {}

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun getFragmentContainer(): Int? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canceller = delayedMainThread(Seconds(2)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        canceller.cancel()
    }

}