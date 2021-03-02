package com.merseyside.ar.sample.main.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.merseyside.ar.R
import com.merseyside.ar.databinding.ActivityWallsBinding
import com.merseyside.ar.sample.base.ArActivity

class WallsActivity : ArActivity<ActivityWallsBinding>() {
    override fun performInjection(bundle: Bundle?) {}

    override fun getLayoutId(): Int {
        return R.layout.activity_walls
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun getFragmentContainer(): Int? {
        return null
    }

    override fun getSurfaceViewId() = getBinding().surfaceView.id

}