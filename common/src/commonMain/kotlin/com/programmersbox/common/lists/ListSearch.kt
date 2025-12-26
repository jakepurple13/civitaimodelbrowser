package com.programmersbox.common.lists

import com.programmersbox.common.db.CustomList

/**
 * Performs a Depth First Search on a list of [com.programmersbox.common.db.CustomList]s.
 * It traverses each [com.programmersbox.common.db.CustomList] and then its [com.programmersbox.common.db.CustomListInfo] items.
 */
fun List<CustomList>.dfsSearch(query: String): List<CustomList> {
    return flatMap { customList ->
        sequence {
            // Visit the parent CustomList (its item)
            if (customList.item.name.contains(query, ignoreCase = true)) {
                yield(customList)
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
                }
            }
        }
    }
}