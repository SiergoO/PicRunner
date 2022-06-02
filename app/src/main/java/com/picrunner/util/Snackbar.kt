package com.picrunner.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.makeEndlessSnackbar(message: String) = Snackbar.make(
    this,
    message,
    Snackbar.LENGTH_INDEFINITE
)

fun View.showErrorSnackbar(t: Throwable) = Snackbar.make(
    this,
    t.message.toString(),
    Snackbar.LENGTH_LONG
).show()