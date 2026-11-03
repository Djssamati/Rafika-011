package com.camera.ta3bia

/**
 * Extracts the 14-digit recharge code from raw OCR lines.
 *
 * The old app took the FIRST 14 digits of whatever the OCR returned, so any
 * serial number / date / phone number printed on the card could end up in the
 * field. Here we only accept a group of digits that is exactly 14 long.
 */
object RechargeParser {
    const val CODE_LENGTH = 14

    /** @param digits the code (or partial digits), @param found how many digits the best candidate had */
    data class Result(val digits: String, val complete: Boolean, val found: Int)

    fun parse(lines: List<String>): Result {
        val runs = lines.map(::lineDigits).filter { it.length >= 2 }

        // 1) one line that holds exactly 14 digits (also "1234 5678 9012 34")
        runs.firstOrNull { it.length == CODE_LENGTH }?.let { return Result(it, true, CODE_LENGTH) }

        // 2) the code was split on 2..4 consecutive lines
        for (size in 2..4) {
            for (i in 0..runs.size - size) {
                val joined = runs.subList(i, i + size).joinToString("")
                if (joined.length == CODE_LENGTH) return Result(joined, true, CODE_LENGTH)
            }
        }

        // 3) the whole text has exactly 14 digits
        val all = runs.joinToString("")
        if (all.length == CODE_LENGTH) return Result(all, true, CODE_LENGTH)

        // 4) incomplete: show what we have so the user can finish by hand
        val best = runs.maxByOrNull { it.length } ?: return Result("", false, 0)
        return if (best.length < CODE_LENGTH) Result(best, false, best.length)
        else Result("", false, best.length) // more than 14 digits -> probably a serial number, don't guess
    }

    /** Keeps only digit-like tokens of a line and fixes common OCR confusions inside them (O->0, I->1 ...). */
    private fun lineDigits(line: String): String =
        line.split(Regex("[\\s\\-.]+")).joinToString("") { token ->
            val digitCount = token.count { Character.digit(it, 10) >= 0 }
            if (token.isNotEmpty() && digitCount >= 1 && digitCount * 2 >= token.length) {
                buildString {
                    for (c in token) {
                        val d = Character.digit(c, 10)
                        when {
                            d >= 0 -> append(d)
                            c == 'O' || c == 'o' -> append('0')
                            c == 'I' || c == 'l' || c == '|' -> append('1')
                            c == 'S' -> append('5')
                            c == 'B' -> append('8')
                        }
                    }
                }
            } else ""
        }

    /** Normalises anything typed/pasted (including Arabic-Indic digits) to 0-9 only, max 14. */
    fun normalizeDigits(input: String): String = buildString {
        for (c in input) {
            val d = Character.digit(c, 10)
            if (d >= 0 && length < CODE_LENGTH) append(d)
        }
    }
}
