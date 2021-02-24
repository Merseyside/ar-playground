package com.merseyside.template.news.main.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.merseyside.archy.presentation.activity.BaseBindingActivity
import com.merseyside.template.R
import com.merseyside.template.databinding.ActivityMainBinding

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    override fun performInjection(bundle: Bundle?) {}

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun getToolbar(): Toolbar {
        return getBinding().toolbar
    }

    override fun getFragmentContainer(): Int? {
        return null
    }
}