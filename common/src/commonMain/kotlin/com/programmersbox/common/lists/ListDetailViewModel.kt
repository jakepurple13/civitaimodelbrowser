package com.programmersbox.common.lists

import androidx.lifecycle.ViewModel
import com.programmersbox.common.db.ListDao

class ListDetailViewModel(
    private val listDao: ListDao,
    uuid: String,
) : ViewModel() {
    val list = listDao.getCustomListItemFlow(uuid)

}