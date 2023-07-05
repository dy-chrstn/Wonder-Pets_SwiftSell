package com.example.scanit


data class transData(val transcationID: Int,
                     val description: String,
                     val itemTotal: String,
                     val itemDate: String,
                     val itemTime: String){

    constructor() : this(0, "", "", "", "")
}


