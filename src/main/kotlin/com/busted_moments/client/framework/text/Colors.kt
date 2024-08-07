package com.busted_moments.client.framework.text

import net.essentuan.esl.color.Color
import net.minecraft.ChatFormatting

const val COLOR_CHAR = '§'
const val BLACK = "${COLOR_CHAR}0"
const val DARK_BLUE = "${COLOR_CHAR}1"
const val DARK_GREEN = "${COLOR_CHAR}2"
const val DARK_AQUA = "${COLOR_CHAR}3"
const val DARK_RED = "${COLOR_CHAR}4"
const val DARK_PURPLE = "${COLOR_CHAR}5"
const val GOLD = "${COLOR_CHAR}6"
const val GRAY = "${COLOR_CHAR}7"
const val DARK_GRAY = "${COLOR_CHAR}8"
const val BLUE = "${COLOR_CHAR}9"
const val GREEN = "${COLOR_CHAR}a"
const val AQUA = "${COLOR_CHAR}b"
const val RED = "${COLOR_CHAR}c"
const val LIGHT_PURPLE = "${COLOR_CHAR}d"
const val YELLOW = "${COLOR_CHAR}e"
const val WHITE = "${COLOR_CHAR}f"
const val OBFUSCATED = "${COLOR_CHAR}k"
const val BOLD = "${COLOR_CHAR}l"
const val STRIKETHROUGH = "${COLOR_CHAR}m"
const val UNDERLINE = "${COLOR_CHAR}n"
const val ITALIC = "${COLOR_CHAR}o"
const val RESET = "${COLOR_CHAR}r"

val ChatFormatting.esl: Color
    get() = Color(color!!, alpha = false)