package dev.gaens.filmory.feature.home.presentation

import dev.gaens.filmory.feature.settings.domain.HomeBlock

const val HOME_GRID_COLUMNS = 2

/**
 * Columns each block takes in the Home grid. Sections span every column, and so does a tile with no
 * partner in its row, so a lone tile never leaves a hole.
 */
fun homeSpans(layout: List<HomeBlock>): Map<HomeBlock, Int> {
    var column = 0
    return layout.withIndex().associate { (index, block) ->
        val hasPartner = column != 0 || layout.getOrNull(index + 1)?.isSection == false
        val span = if (block.isSection || !hasPartner) HOME_GRID_COLUMNS else 1
        column = (column + span) % HOME_GRID_COLUMNS
        block to span
    }
}

/** Moves [from] to the place [to] holds now, shifting the blocks in between. */
fun List<HomeBlock>.move(from: HomeBlock, to: HomeBlock): List<HomeBlock> {
    val target = indexOf(to)
    if (from == to || target < 0 || from !in this) return this
    return toMutableList().apply { add(target, removeAt(indexOf(from))) }
}

/** Moves [block] one place up or down. Used by the accessibility actions. */
fun List<HomeBlock>.moveBy(block: HomeBlock, offset: Int): List<HomeBlock> =
    getOrNull(indexOf(block) + offset)?.let { move(block, it) } ?: this
