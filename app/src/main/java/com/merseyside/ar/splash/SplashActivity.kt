package com.merseyside.ar.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.merseyside.ar.R
import com.merseyside.ar.databinding.ActivitySplashBinding
import com.merseyside.ar.sample.main.view.WallsActivity
import com.merseyside.archy.presentation.activity.BaseBindingActivity
import com.merseyside.utils.HandlerCanceller
import com.merseyside.utils.delayedMainThread
import com.merseyside.utils.time.Seconds

class SplashActivity : BaseBindingActivity<ActivitySplashBinding>() {

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
            val intent = Intent(this, WallsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        canceller.cancel()
    }

}