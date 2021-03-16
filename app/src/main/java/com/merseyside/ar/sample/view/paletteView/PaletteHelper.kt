package com.merseyside.ar.sample.view.paletteView

import com.merseyside.ar.R


class PaletteHelper {

    companion object {

        private val DEFAULT_COLOR = PaletteColor("default", R.color.palette_default)

        private val palette = listOf(
            DEFAULT_COLOR,
            PaletteColor("yellow", R.color.palette_yellow),
            PaletteColor("red", R.color.palette_red),
            PaletteColor("green", R.color.palette_green),
            PaletteColor("purple", R.color.palette_dark_purple),
            PaletteColor("cyan", R.color.palette_cyan),
            PaletteColor("pink", R.color.palette_pink),
            PaletteColor("orange", R.color.palette_orange),
            PaletteColor("blue", R.color.palette_blue),
            PaletteColor("cyan_dark", R.color.palette_cyan_dark)
        )

        fun getPaletteList(): List<PaletteColor> {
            return palette
        }

        fun getColorById(id: String): PaletteColor {
            return palette.find { it.id == id } ?: DEFAULT_COLOR
        }
    }
}