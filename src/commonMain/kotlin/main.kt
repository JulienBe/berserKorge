import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.font.*
import com.soywiz.korim.format.*
import com.soywiz.korim.text.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import julien.*
import kotlin.math.*
import kotlin.random.*

val blocks = mutableMapOf<Int, Block>()
var freeId = 0
var map = PositionMap()
var isAnimationRunning = false
var isGameOver = false

fun columnX(number: Int, cellSize: Double, leftIndent: Int) = leftIndent + 10 + (cellSize + 10) * number
fun rowY(number: Int, cellSize: Double, topIndent: Int) = topIndent + 10 + (cellSize + 10) * number

suspend fun main() = Korge(width = 480, height = 640, bgcolor = Colors["#2b2b2b"]) {
    val font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
    val k1 = resourcesVfs["k1.JPG"].readBitmap()
    val k2 = resourcesVfs["k2.JPG"].readBitmap()
    val blockImages = BlockColor.values().map { resourcesVfs["${it.name.lowercase()}.jpg"].readBitmap() }

    val cellSize: Int = views.virtualWidth / 5
    val fieldSize: Double = 50.0 + cellSize * 4
    val leftIndent: Double = (views.virtualWidth - fieldSize) / 2.0
    val topIndent: Double = 150.0

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
    val bgLogo = roundRect(cellSize.toDouble(), cellSize.toDouble(), 5.0, fill = Colors["#ff1600"]) {
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

    text("Emrys\nparty", cellSize * 0.35, Colors["#090a0b"], font).centerOn(bgLogo)
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
        onClick {
            this@Korge.restart(cellSize.toDouble(), font, leftIndent.roundToInt(), topIndent.roundToInt(), blockImages)
        }
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

    generateBlock(cellSize.toDouble(), font, leftIndent.roundToInt(), topIndent.roundToInt(), blockImages)

    keys {
        down {
            when (it.key) {
                Key.LEFT -> moveBlocksTo(cellSize.toDouble(), font, fieldSize, leftIndent.roundToInt(), topIndent.roundToInt(), Direction.LEFT, blockImages)
                Key.RIGHT -> moveBlocksTo(cellSize.toDouble(), font, fieldSize, leftIndent.roundToInt(), topIndent.roundToInt(), Direction.RIGHT, blockImages)
                Key.UP -> moveBlocksTo(cellSize.toDouble(), font, fieldSize, leftIndent.roundToInt(), topIndent.roundToInt(), Direction.TOP, blockImages)
                Key.DOWN -> moveBlocksTo(cellSize.toDouble(), font, fieldSize, leftIndent.roundToInt(), topIndent.roundToInt(), Direction.BOTTOM, blockImages)
                else -> Unit
            }
        }
    }
}

fun Stage.showAnimation(cellSize: Double, font: Font, fieldSize: Double, leftIndent: Int, topIndent: Int, blockImages: List<Bitmap>, moves: List<Pair<Int, Position>>, merges: List<Triple<Int, Int, Position>>, onEnd: () -> Unit) = launchImmediately {
    animateSequence {
        parallel {
            moves.forEach { (id, pos) ->
                blocks[id]!!.moveTo(columnX(pos.x, cellSize, leftIndent), rowY(pos.y, cellSize, topIndent), 0.15.seconds, Easing.LINEAR)
            }
            merges.forEach { (id1, id2, pos) ->
                sequence {
                    parallel {
                        blocks[id1]!!.moveTo(columnX(pos.x, cellSize, leftIndent), rowY(pos.y, cellSize, topIndent), 0.15.seconds, Easing.LINEAR)
                        blocks[id2]!!.moveTo(columnX(pos.x, cellSize, leftIndent), rowY(pos.y, cellSize, topIndent), 0.15.seconds, Easing.LINEAR)
                    }
                    block {
                        val nextNumber = numberFor(id1).next()
                        deleteBlock(id1)
                        deleteBlock(id2)
                        createNewBlockWithId(id1, nextNumber, pos, cellSize, font, leftIndent, topIndent, blockImages[nextNumber.ordinal])
                    }
                    sequenceLazy {
                        animateScale(blocks[id1]!!)
                    }
                }
            }
        }
        block {
            onEnd()
        }
    }
}

fun numberFor(blockId: Int): BlockColor = blocks[blockId]!!.color
fun deleteBlock(blockId: Int) = blocks.remove(blockId)!!.removeFromParent()

fun Animator.animateScale(block: Block) {
    val x = block.x
    val y = block.y
    val scale = block.scale
    tween(
        block::x[x - 4],
        block::y[y - 4],
        block::scale[scale + 0.1],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
    tween(
        block::x[x],
        block::y[y],
        block::scale[scale],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
}

fun Stage.moveBlocksTo(cellSize: Double, font: Font, fieldSize: Double, leftIndent: Int, topIndent: Int, direction: Direction, blockImages: List<Bitmap>) {
    if (isAnimationRunning) return
    if (!map.hasAvailableMoves()) {
        if (!isGameOver) {
            isGameOver = true
            showGameOver(font, fieldSize, leftIndent, topIndent) {
                isGameOver = false
                restart(cellSize, font, leftIndent, topIndent, blockImages)
            }
        }
        return
    }
    val moves = mutableListOf<Pair<Int, Position>>()
    val merges = mutableListOf<Triple<Int, Int, Position>>()

    val newMap = map.calculateNewMap(direction, moves, merges) { blockId ->
        numberFor(blockId)
    }

    if (map != newMap) {
        isAnimationRunning = true
        showAnimation(cellSize, font, fieldSize, leftIndent, topIndent, blockImages, moves, merges) {
            // when animation ends
            map = newMap
            generateBlock(cellSize, font, leftIndent, topIndent, blockImages)
            isAnimationRunning = false
        }
    }
}
fun Container.showGameOver(font: Font, fieldSize: Double, leftIndent: Int, topIndent: Int, onRestart: () -> Unit) = container {
//    val format = TextFormat(
//        color = RGBA(0, 0, 0),
//        size = 40,
//        font = font
//    )
//    val skin = TextSkin(
//        normal = format,
//        over = format.copy(color = RGBA(90, 90, 90)),
//        down = format.copy(color = RGBA(120, 120, 120))
//    )

    fun restart() {
        this@container.removeFromParent()
        onRestart()
    }

    position(leftIndent, topIndent)

    roundRect(fieldSize, fieldSize, 5.0, fill = Colors["#FFFFFF33"])
    text("Game Over", 60.0, Colors.BLACK, font) {
        centerBetween(0.0, 0.0, fieldSize, fieldSize)
        y -= 60
    }
//    uiText("Try again", 120.0, 35.0, skin) {
//        centerBetween(0.0, 0.0, fieldSize, fieldSize)
//        y += 20
//        onClick { restart() }
//    }

    keys.down {
        when (it.key) {
            Key.ENTER, Key.SPACE -> restart()
            else -> Unit
        }
    }
}

fun Container.restart(cellSize: Double, font: Font, leftIndent: Int, topIndent: Int, blockImages: List<Bitmap>) {
    map = PositionMap()
    blocks.values.forEach { it.removeFromParent() }
    blocks.clear()
    generateBlock(cellSize, font, leftIndent, topIndent, blockImages)
}

fun Container.createNewBlockWithId(id: Int, color: BlockColor, pos: Position, cellSize: Double, font: Font, leftIndent: Int, topIndent: Int, backgroundBlock: Bitmap) {
    blocks[id] = block(color, cellSize, font, backgroundBlock).position(columnX(pos.x, cellSize, leftIndent), rowY(pos.y, cellSize, topIndent))
}
fun Container.createNewBlock(color: BlockColor, pos: Position, cellSize: Double, font: Font, leftIndent: Int, topIndent: Int, backgroundBlock: Bitmap): Int {
    val id = freeId++
    createNewBlockWithId(id, color, pos, cellSize, font, leftIndent, topIndent, backgroundBlock)
    return id
}

fun Container.generateBlock(cellSize: Double, font: Font, leftIndent: Int, topIndent: Int, blockImages: List<Bitmap>) {
    val position = map.getRandomFreePosition() ?: return
    val number = if (Random.nextDouble() < 0.9) BlockColor.ZERO else BlockColor.ONE
    val newId = createNewBlock(number, position, cellSize, font, leftIndent, topIndent, blockImages[number.ordinal])
    map[position.x, position.y] = newId
}
