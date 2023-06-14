package com.example.scanit

data class buyModel(
    val TransactionID: String,
    val itemBarcode: String,
    val itemName: String,
    val itemQuantity: Int,
    val itemPrice: Double,
    val itemTotal: Double
)
