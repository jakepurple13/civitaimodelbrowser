package com.programmersbox.common.presentation.lists

import com.programmersbox.common.db.CustomList
import kotlinx.serialization.Serializable

@Serializable
enum class SearchType {
    DFS, BFS
}

/**
 * Performs a Breadth First Search on a list of [com.programmersbox.common.db.CustomList]s.
 * It first visits all [com.programmersbox.common.db.CustomList]s and then their [com.programmersbox.common.db.CustomListInfo] items.
 */
fun List<CustomList>.bfsSearch(query: String): List<CustomList> {
    return sequence {
        val addedList = mutableListOf<CustomList>()
        // First level: Visit all parent CustomLists (their items)
        forEach { customList ->
            if (customList.item.name.contains(query, ignoreCase = true)) {
                yield(customList)
                addedList.add(customList)
            }
        }
        // Second level: Visit all children (CustomListInfo items) across all CustomLists
        forEach { customList ->
            if (customList in addedList) return@forEach
            customList.list.forEach { item ->
                if (
                    item.name.contains(query, ignoreCase = true) ||
                    item.description?.contains(query, ignoreCase = true) == true
                ) {
                    yield(
                        customList.copy(
                            list = customList.list
                                .sortedByDescending {
                                    it.name.contains(query, ignoreCase = true) ||
                                            it.description
                                                ?.contains(query, ignoreCase = true) == true
                                }
                        )
                    )
                    return@forEach
                }
            }
        }
    }.toList()
}

/**
 * Performs a Depth First Search on a list of [com.programmersbox.common.db.CustomList]s.
 * It traverses each [com.programmersbox.common.db.CustomList] and then its [com.programmersbox.common.db.CustomListInfo] items.
 */
fun List<CustomList>.dfsSearch(query: String): List<CustomList> {
    return sequence {
        loop@ for (customList in this@dfsSearch) {
            // Visit the parent CustomList (its item)
            if (customList.item.name.contains(query, ignoreCase = true)) {
                yield(customList)
                continue
            }
            // Visit children (CustomListInfo items)
            customList.list.forEach { item ->
                if (
                    item.name.contains(query, ignoreCase = true) ||
                    item.description?.contains(query, ignoreCase = true) == true
                ) {
                    yield(
                        customList.copy(
                            list = customList.list
                                .sortedByDescending {
                                    it.name.contains(query, ignoreCase = true) ||
                                            it.description
                                                ?.contains(query, ignoreCase = true) == true
                                }
                        )
                    )
                    continue@loop
                }
            }
        }
    }.toList()
}