package com.example.stockdash.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes


fun Context.resolveThemeColor(@AttrRes colorAttr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}