package com.busted_moments.client.framework.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale


object Numbers {
    private const val ZERO_WIDTH: Char = '\u200C'
    private val SYMBOLS: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    private val DOUBLE_FORMATTER: NumberFormat = DecimalFormat("#,###.00", SYMBOLS)
    private val LONG_FORMATTER: NumberFormat = DecimalFormat("#,###", SYMBOLS)

    fun String.escapeCommas(): String =
        replace(",", "$ZERO_WIDTH,$ZERO_WIDTH")

    fun String.unescapeCommas(): String =
        replace("$ZERO_WIDTH,$ZERO_WIDTH", ",")

    fun Int.toCommaString(): String =
        LONG_FORMATTER.format(this.toLong())

    fun Long.toCommaString(): String =
        LONG_FORMATTER.format(this)

    fun Float.toCommaString(): String =
        DOUBLE_FORMATTER.format(this.toDouble())

    fun Double.toCommaString(): String =
        DOUBLE_FORMATTER.format(this)
}