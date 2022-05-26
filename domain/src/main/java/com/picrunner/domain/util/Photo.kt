package com.picrunner.domain.util

import com.picrunner.domain.model.Photo

private const val URL_SCHEME = "https://"
private const val DOMAIN = "live.staticflickr.com"
private const val PHOTO_FORMAT = "jpg"

fun Photo.convertToStringUrl(): String =
    "$URL_SCHEME$DOMAIN/${this.server}/${this.id}_${this.secret}_z.$PHOTO_FORMAT"
