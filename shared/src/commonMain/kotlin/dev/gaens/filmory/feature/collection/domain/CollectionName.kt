package dev.gaens.filmory.feature.collection.domain

/** [base] when no collection has it, otherwise [base] with the first free number from 2: "New collection 2". */
fun uniqueCollectionName(base: String, taken: Collection<String>): String {
    val used = taken.mapTo(HashSet()) { it.trim().lowercase() }
    if (base.trim().lowercase() !in used) return base
    var number = 2
    while ("$base $number".lowercase() in used) number++
    return "$base $number"
}
