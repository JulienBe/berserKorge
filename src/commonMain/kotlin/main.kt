import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.font.*
import com.soywiz.korim.format.*
import com.soywiz.korim.text.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*

suspend fun main() = Korge(width = 480, height = 640, bgcolor = Colors["#2b2b2b"]) {
    val font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
    val k1 = resourcesVfs["k1.JPG"].readBitmap()
    val k2 = resourcesVfs["k2.JPG"].readBitmap()
    val k3 = resourcesVfs["k3.JPG"].readBitmap()

    val cellSize: Int = views.virtualWidth / 5
    val fieldSize = 50.0 + cellSize * 4
    val leftIndent = (views.virtualWidth - fieldSize) / 2.0
    val topIndent = 150.0

    val bgField = roundRect(fieldSize, fieldSize, 10.0, fill = Colors["#1b1b1b"], stroke = Colors["#3b3b3b"], strokeThickness = 5.0) {
        position(leftIndent, topIndent)
    }

	val sceneContainer = sceneContainer()
    // CELLS
    graphics {
        it.position(leftIndent, topIndent)
        fill(Colors["#cec0b2"]) {
            for (i in 0..3) {
                val x: Int = 10 + (10 + cellSize) * i
                for (j in 0..3) {
                    val y: Int = 10 + (10 + cellSize) * j
                    it.roundRect(cellSize, cellSize, x, y, fill = Colors["#cec0b2"], stroke = Colors["#3b3b3b"]) {
                        position(x, y)
                    }
                }
            }
        }
    }
    // UI ON TOP
    val bgLogo = roundRect(cellSize.toDouble(), cellSize.toDouble(), 5.0, fill = Colors["#edc403"]) {
        position(leftIndent, 5.0)
    }

    val bgBest = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = Colors["#bbae9e"]) {
        alignRightToRightOf(bgField)
        alignTopToTopOf(bgLogo)
    }
    val bgScore = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = Colors["#bbae9e"]) {
        alignRightToLeftOf(bgBest, 24.0)
        alignTopToTopOf(bgBest)
    }

    text("Emrys\nGame", cellSize * 0.35, Colors["#090a0b"], font).centerOn(bgLogo)
    text("BEST", cellSize * 0.25, RGBA(239, 226, 210), font) {
        centerXOn(bgBest)
        alignTopToTopOf(bgBest, 5.0)
    }
    text("0", cellSize * 0.5, Colors.WHITE, font) {
        setTextBounds(Rectangle(0.0, 0.0, bgBest.width, cellSize - 24.0))
        alignment = TextAlignment.MIDDLE_CENTER
        alignTopToTopOf(bgBest, 12.0)
        centerXOn(bgBest)
    }
    text("SCORE", cellSize * 0.25, RGBA(239, 226, 210), font) {
        centerXOn(bgScore)
        alignTopToTopOf(bgScore, 5.0)
    }
    text("0", cellSize * 0.5, Colors.WHITE, font) {
        setTextBounds(Rectangle(0.0, 0.0, bgScore.width, cellSize - 24.0))
        alignment = TextAlignment.MIDDLE_CENTER
        centerXOn(bgScore)
        alignTopToTopOf(bgScore, 12.0)
    }
    // BUTTONS
    val buttonSize = cellSize * 0.6
    val restartBlock = container {
        val background = roundRect(buttonSize, buttonSize, 5.0, fill = Colors["#bbae9e"])
        image(k1) {
            size(buttonSize * 0.9, buttonSize * 0.9)
            centerOn(background)
        }
        alignTopToBottomOf(bgBest, 5)
        alignRightToRightOf(bgField)
    }
    val undoBlock = container {
        val background = roundRect(buttonSize, buttonSize, 5.0, fill = Colors["#bbae9e"])
        image(k2) {
            size(buttonSize * 0.9, buttonSize * 0.9)
            centerOn(background)
        }
        alignTopToTopOf(restartBlock)
        alignRightToLeftOf(restartBlock, 5)
    }
}
