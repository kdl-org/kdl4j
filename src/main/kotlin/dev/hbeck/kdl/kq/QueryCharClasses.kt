package dev.hbeck.kdl.kq

import dev.hbeck.kdl.parse.CharClasses


class QueryCharClasses {
    companion object {
        fun isNumericPredicateStart(c: Int): Boolean =
                when (c) {
                    '='.toInt(), '>'.toInt(), '<'.toInt() -> true
                    else -> false
                }

        fun isValidQueryBareIdChar(c: Int): Boolean {
            return when (c) {
                '\n'.toInt(),
                '\u000C'.toInt(),
                '\r'.toInt(),
                '\u0085'.toInt(),
                '\u2028'.toInt(),
                '\u2029'.toInt(),
                '\\'.toInt(),
                '{'.toInt(),
                '}'.toInt(),
                '<'.toInt(),
                '>'.toInt(),
                ';'.toInt(),
                '['.toInt(),
                ']'.toInt(),
                '('.toInt(),
                ')'.toInt(),
                '='.toInt(),
                ','.toInt(),
                '.'.toInt(),
                '"'.toInt(),
                '\u0009'.toInt(),
                '\u0020'.toInt(),
                '\u00A0'.toInt(),
                '\u1680'.toInt(),
                '\u2000'.toInt(),
                '\u2001'.toInt(),
                '\u2002'.toInt(),
                '\u2003'.toInt(),
                '\u2004'.toInt(),
                '\u2005'.toInt(),
                '\u2006'.toInt(),
                '\u2007'.toInt(),
                '\u2008'.toInt(),
                '\u2009'.toInt(),
                '\u200A'.toInt(),
                '\u202F'.toInt(),
                '\u205F'.toInt(),
                '\u3000'.toInt() -> false
                else -> true
            }
        }

        fun isValidQueryBareIdStart(c: Int): Boolean {
            return !CharClasses.isValidDecimalChar(c) && isValidQueryBareIdChar(c)
        }
    }
}