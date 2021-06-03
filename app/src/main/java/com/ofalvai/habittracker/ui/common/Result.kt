package com.ofalvai.habittracker.ui.common

sealed class Result<out T> {
    class Success<R>(val value: R) : Result<R>()
    object Loading : Result<Nothing>()
    class Failure(error: Throwable) : Result<Nothing>()
}