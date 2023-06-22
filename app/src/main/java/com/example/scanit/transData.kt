package com.example.scanit

data class transData(val transcationID: Int,
                     val description: String,
                     val itemTotal: Double){
    constructor(): this(0,"",0.0)
}
