package com.example.bookshelfapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import retrofit2.HttpException

class BooksViewModel(private val repository: BooksRepository) : ViewModel() {

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 5
        const val DEFAULT_RETRY_DELAY_MS = 2000L
    }

    private val _books = MutableLiveData<List<BookItem>>()
    val books: LiveData<List<BookItem>> get() = _books

    fun searchBooks(query: String) {
        viewModelScope.launch {
            retrySearchBooks(query)
        }
    }

    private suspend fun retrySearchBooks(query: String) {
        var attempt = 0

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                val fetchedBooks = repository.searchBooks(query) ?: emptyList()
                _books.postValue(fetchedBooks.take(6))
                return
            } catch (exception: HttpException) {
                if (exception.code() == 429) {
                    attempt++
                    handleRetryDelay(attempt, exception)
                } else {
                    handleFailure()
                    return
                }
            }
        }
        handleFailure()
    }

    private suspend fun handleRetryDelay(attempt: Int, exception: HttpException) {
        val retryAfterSeconds = exception.response()?.headers()?.get("Retry-After")?.toLongOrNull()
        val delayDuration = retryAfterSeconds?.times(1000) ?: (DEFAULT_RETRY_DELAY_MS * attempt)

        if (attempt < MAX_RETRY_ATTEMPTS) {
            delay(delayDuration)
        }
    }

    private fun handleFailure() {
        _books.postValue(emptyList())
    }
}
