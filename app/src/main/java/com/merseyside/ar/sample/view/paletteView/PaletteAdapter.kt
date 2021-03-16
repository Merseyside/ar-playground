package com.merseyside.ar.sample.view.paletteView

import android.view.ViewGroup
import com.merseyside.adapters.base.BaseSelectableAdapter
import com.merseyside.adapters.view.TypedBindingHolder
import com.merseyside.ar.R
import com.merseyside.ar.BR
import com.merseyside.ar.databinding.ViewPaletteItemBinding

class PaletteAdapter : BaseSelectableAdapter<PaletteColor, PaletteItemViewModel>() {

    private var colorSize = 0
    private var checkSize = 0
    private var padding = 0

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.view_palette_item
    }

    override fun getBindingVariable(): Int {
        return BR.obj
    }

    override fun createItemViewModel(obj: PaletteColor): PaletteItemViewModel {
        return PaletteItemViewModel(obj)
    }

    fun setSizes(colorSize: Int, checkSize: Int) {
        this.colorSize = colorSize
        this.checkSize = checkSize
    }

    fun setPadding(padding: Int) {
        this.padding = padding
    }

    override fun onBindViewHolder(holder: TypedBindingHolder<PaletteItemViewModel>, position: Int) {

        if (colorSize != 0 && checkSize != 0) {
            val binding = holder.binding as ViewPaletteItemBinding

            binding.colorView.layoutParams.apply {
                height = colorSize
                width = colorSize
            }

            binding.check.layoutParams.apply {
                height = checkSize
                width = checkSize
            }

            binding.colorView.apply {
                val layoutParams = layoutParams

                layoutParams as ViewGroup.MarginLayoutParams

                layoutParams.setMargins(padding, padding, padding, padding)
                this.layoutParams = layoutParams
            }
        }

        super.onBindViewHolder(holder, position)
    }
}