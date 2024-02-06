package com.example.myapplication.models;

import Movie

data class Posts(
    val userId: String = "",
    val movie: Movie = Movie(),
    val review: String = "",
    val rating: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {

}