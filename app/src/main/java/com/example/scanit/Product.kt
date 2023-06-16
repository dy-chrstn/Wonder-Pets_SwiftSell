package com.example.scanit

data class Product(
    val itemBarcode: String,
    val itemCategory: String,
    val itemName: String,
    val itemExpiry: String,
    val itemPrice: Int,
    val itemCost: Int,
    val itemQuantity: Int,
    var imageUrl: String // To store the URL of the image
)

