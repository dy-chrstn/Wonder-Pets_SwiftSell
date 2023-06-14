package com.example.scanit

import androidx.lifecycle.ViewModel
import com.example.scanit.buyModel

class buyViewModel : ViewModel() {
    private val itemList = ArrayList<buyModel>()

    // Add item to the list
    fun addItem(item: buyModel) {
        itemList.add(item)
    }

    // Get the list of items
    fun getItems(): List<buyModel> {
        return itemList
    }
}
