package com.picrunner.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showEndlessSnackbar(message: String) = Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)