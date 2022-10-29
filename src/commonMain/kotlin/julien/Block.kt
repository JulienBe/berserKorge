package julien

import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.font.*

class Block(val color: BlockColor, val cellSize: Double, val font: Font, val background: Bitmap) : Container() {
    init {
        roundRect(cellSize, cellSize, 5.0, fill = color.rgb)
        image(background) {
            size(cellSize * 0.95, cellSize * 0.95)
        }
    }
}

fun Container.block(color: BlockColor, cellSize: Double, font: Font, background: Bitmap) = Block(color, cellSize, font, background).addTo(this)
