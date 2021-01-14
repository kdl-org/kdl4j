package dev.hbeck.kdl.kq


class QueryCharClasses {
    companion object {
        fun isNumericPredicateStart(c: Int): Boolean =
                when (c) {
                    '='.toInt(), '>'.toInt(), '<'.toInt() -> true
                    else -> false
                }

    }
}