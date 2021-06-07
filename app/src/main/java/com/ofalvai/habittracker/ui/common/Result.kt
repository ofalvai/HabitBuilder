package com.ofalvai.habittracker.ui.common

sealed class Result<out T> {
    data class Success<R>(val value: R) : Result<R>()
    object Loading : Result<Nothing>()
    data class Failure(val error: Throwable) : Result<Nothing>()
}