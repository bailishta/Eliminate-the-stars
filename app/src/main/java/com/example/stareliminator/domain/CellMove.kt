package com.example.stareliminator.domain

data class CellMove(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val color: Int
)
