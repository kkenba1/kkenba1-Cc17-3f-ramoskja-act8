package com.example.bookshelfapp

data class BookResponse(
    val items: List<BookItem>?
)

data class ImageLinks(
    val thumbnail: String
)

data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val imageLinks: ImageLinks?
)

