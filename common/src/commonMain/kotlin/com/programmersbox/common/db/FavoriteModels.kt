package com.programmersbox.common.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class FavoriteList : RealmObject {
    var favorites = realmListOf<Favorite>()
}

class Favorite : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var name: String = ""
    var description: String? = null
    var type: String = "Other"
    var nsfw: Boolean = false
    var imageUrl: String? = null
}