package com.merseyside.ar.sample.view.paletteView

import androidx.annotation.ColorRes
import androidx.databinding.Bindable
import com.merseyside.adapters.model.BaseSelectableAdapterViewModel

class PaletteItemViewModel(obj: PaletteColor)
    : BaseSelectableAdapterViewModel<PaletteColor>(obj) {

    override fun areContentsTheSame(obj: PaletteColor): Boolean {
        return false
    }

    override fun compareTo(obj: PaletteColor): Int {
        return 0
    }

    override fun areItemsTheSame(obj: PaletteColor): Boolean {
        return this.obj.id == obj.id
    }

    override fun notifyUpdate() {}

    @ColorRes
    @Bindable
    fun getColor(): Int {
        return obj.resId
    }

    override fun onSelectedChanged(isSelected: Boolean) {}

    override fun notifySelectEnabled(isEnabled: Boolean) {}

}