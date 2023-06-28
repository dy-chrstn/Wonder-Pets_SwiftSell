package com.example.scanit

import java.time.LocalDate
import java.util.Date

data class transData(val transcationID: Int,
                     val description: String,
                     val itemTotal: String,
                     val itemDate: String,
                     val itemTime: String){

    constructor() : this(0, "", "", "", "")
}


