package com.example.scanit

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val itemBarcode: String,
    val itemCategory: String,
    val itemName: String,
    val itemExpiry: String,
    val itemPrice: Int,
    val itemCost: Int,
    val itemQuantity: Int,
    var imageUrl: String // To store the URL of the image
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemBarcode)
        parcel.writeString(itemCategory)
        parcel.writeString(itemName)
        parcel.writeString(itemExpiry)
        parcel.writeInt(itemPrice)
        parcel.writeInt(itemCost)
        parcel.writeInt(itemQuantity)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}

