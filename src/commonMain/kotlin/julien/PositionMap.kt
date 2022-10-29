package julien

import blocks
import com.soywiz.kds.*
import kotlin.random.*

class Position(val x: Int, val y: Int)

class PositionMap(private val array: IntArray2 = IntArray2(4, 4, -1)) {

    fun calculateNewMap(
        direction: Direction,
        moves: MutableList<Pair<Int, Position>>,
        merges: MutableList<Triple<Int, Int, Position>>,
        numberFor: (Int) -> BlockColor
    ): PositionMap {
        val newMap = PositionMap()
        val startIndex = when (direction) {
            Direction.LEFT, Direction.TOP -> 0
            Direction.RIGHT, Direction.BOTTOM -> 3
        }
        var columnRow = startIndex

        fun newPosition(line: Int) = when (direction) {
            Direction.LEFT -> Position(columnRow++, line)
            Direction.RIGHT -> Position(columnRow--, line)
            Direction.TOP -> Position(line, columnRow++)
            Direction.BOTTOM -> Position(line, columnRow--)
        }

        for (line in 0..3) {
            var curPos = this.getNotEmptyPositionFrom(direction, line)
            columnRow = startIndex
            while (curPos != null) {
                val newPos = newPosition(line)
                val curId = this[curPos.x, curPos.y]
                this[curPos.x, curPos.y] = -1

                val nextPos = this.getNotEmptyPositionFrom(direction, line)
                val nextId = nextPos?.let { this[it.x, it.y] }
                //two blocks are equal
                if (nextId != null && numberFor(curId) == numberFor(nextId)) {
                    //merge these blocks
                    this[nextPos.x, nextPos.y] = -1
                    newMap[newPos.x, newPos.y] = curId
                    merges += Triple(curId, nextId, newPos)
                } else {
                    //add old block
                    newMap[newPos.x, newPos.y] = curId
                    moves += Pair(curId, newPos)
                }
                curPos = this.getNotEmptyPositionFrom(direction, line)
            }
        }
        return newMap
    }

    private fun getNotEmptyPositionFrom(direction: Direction, line: Int): Position? {
        when (direction) {
            Direction.LEFT -> for (i in 0..3) getOrNull(i, line)?.let { return it }
            Direction.RIGHT -> for (i in 3 downTo 0) getOrNull(i, line)?.let { return it }
            Direction.TOP -> for (i in 0..3) getOrNull(line, i)?.let { return it }
            Direction.BOTTOM -> for (i in 3 downTo 0) getOrNull(line, i)?.let { return it }
        }
        return null
    }

    private fun getOrNull(x: Int, y: Int) = if (array.get(x, y) != -1) Position(x, y) else null

    private fun getNumber(x: Int, y: Int) = array.tryGet(x, y)?.let { blocks[it]?.color?.ordinal ?: -1 } ?: -1

    operator fun get(x: Int, y: Int) = array[x, y]

    operator fun set(x: Int, y: Int, value: Int) {
        array[x, y] = value
    }

    fun forEach(action: (Int) -> Unit) { array.forEach(action) }
    fun getRandomFreePosition(): Position? {
        val quantity = array.count { it == -1 }
        if (quantity == 0) return null
        val chosen = Random.nextInt(quantity)
        var current = -1
        array.each { x, y, value ->
            if (value == -1) {
                current++
                if (current == chosen) {
                    return Position(x, y)
                }
            }
        }
        return null
    }

    fun hasAvailableMoves(): Boolean {
        array.each { x, y, _ ->
            if (hasAdjacentEqualPosition(x, y)) return true
        }
        return false
    }

    private fun hasAdjacentEqualPosition(x: Int, y: Int) = getNumber(x, y).let {
        it == getNumber(x - 1, y) || it == getNumber(x + 1, y) || it == getNumber(x, y - 1) || it == getNumber(x, y + 1)
    }

    fun copy() = PositionMap(array.copy(data = array.data.copyOf()))

    override fun equals(other: Any?): Boolean {
        return (other is PositionMap) && this.array.data.contentEquals(other.array.data)
    }

    override fun hashCode() = array.hashCode()

}

enum class Direction {
    LEFT, RIGHT, TOP, BOTTOM
}
