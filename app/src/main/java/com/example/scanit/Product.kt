package com.example.scanit

data class Product(
    val itemName: String,
    val itemPrice: Int,
    val itemQuantity: Int,
    val itemImage: String,
    val itemCategory: Int?,
    val itemCost: String?,
    val itemBarcode: String?,
    val itemExpiry: String?
)
