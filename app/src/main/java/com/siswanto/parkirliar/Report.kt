package com.siswanto.parkirliar

data class Report(
    val location: String = "",
    val description: String = "",
    val latitude : Double,
    val longitude : Double,
    val imageUrl: String = ""
)
