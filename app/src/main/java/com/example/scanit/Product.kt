package com.example.scanit

import java.io.Serializable

data class Product(
    val itemName: String,
    val itemPrice: Int,
    val itemQuantity: Int,
    val itemImage: String,
    val itemCategory: String,
    val itemCost: Int,
    val itemBarcode: String,
    val itemExpiry: String
) : Serializable
