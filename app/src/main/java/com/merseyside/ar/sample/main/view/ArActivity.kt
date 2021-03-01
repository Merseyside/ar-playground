package com.merseyside.ar.sample.main.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.merseyside.ar.R
import com.merseyside.ar.databinding.ActivityArBinding
import com.merseyside.ar.sample.base.ArActivity

class ArActivity : ArActivity<ActivityArBinding>() {
    override fun performInjection(bundle: Bundle?) {}

    override fun getLayoutId(): Int {
        return R.layout.activity_ar
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun getFragmentContainer(): Int? {
        return null
    }

}