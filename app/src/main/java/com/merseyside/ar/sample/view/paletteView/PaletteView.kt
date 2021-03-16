package com.merseyside.ar.sample.view.paletteView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.merseyside.adapters.base.onItemSelected
import com.merseyside.ar.R
import com.merseyside.utils.convertDpToPixel
import com.merseyside.utils.layout.GridAutofitLayoutManager

class PaletteView(context: Context, attributeSet: AttributeSet) :
    RecyclerView(context, attributeSet) {

    private var itemSize: Int = 0
    private var checkSize: Int = 0
    private var itemPaddingSize: Int = 0

    private var rowCount: Int = 0

    private val paletteAdapter = PaletteAdapter()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        loadAttrs(attributeSet)
    }

    private fun loadAttrs(attributeSet: AttributeSet) {
        val array =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.PaletteView, 0, 0)

        itemSize = array.getDimensionPixelSize(
            R.styleable.PaletteView_colorViewSize,
            convertDpToPixel(context, defaultItemSize)
        )
        checkSize = array.getDimensionPixelSize(
            R.styleable.PaletteView_checkSize,
            convertDpToPixel(context, defaultCheckSize)
        )
        itemPaddingSize = array.getDimensionPixelSize(R.styleable.PaletteView_itemPadding, 0)
    }

    fun setPalette(palette: List<PaletteColor>, onSelected: (paletteColor: PaletteColor) -> Unit) {

        if (layoutManager == null) {
            setLayoutManager()
        }

        paletteAdapter.setSizes(itemSize, checkSize)
        paletteAdapter.setPadding(itemPaddingSize)

        paletteAdapter.add(palette)

        adapter = paletteAdapter

        paletteAdapter.onItemSelected { item, isSelected, _ ->
            if (isSelected) {
                onSelected.invoke(item)
            }
        }
    }

    private fun setLayoutManager() {
        layoutManager =
            GridAutofitLayoutManager(
                context,
                calculateItemViewWidthSize(),
                GridLayoutManager.VERTICAL,
                false,
                5
            )
    }

    private fun calculateItemViewWidthSize(): Int {
        return itemSize + itemPaddingSize * 2
    }

    fun getSelected(): PaletteColor = paletteAdapter.getSelectedItem()!!

    fun setSelected(color: PaletteColor) {
        paletteAdapter.selectItem(color)
    }

    fun isEmpty() = paletteAdapter.isEmpty()

    companion object {
        private const val defaultItemSize = 36F
        private const val defaultCheckSize = 18F
    }
}