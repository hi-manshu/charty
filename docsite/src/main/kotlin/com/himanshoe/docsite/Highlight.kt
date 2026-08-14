package com.himanshoe.docsite

private val keywords =
    setOf(
        "as",
        "break",
        "by",
        "class",
        "companion",
        "const",
        "continue",
        "data",
        "do",
        "else",
        "enum",
        "false",
        "for",
        "fun",
        "if",
        "import",
        "in",
        "interface",
        "internal",
        "is",
        "it",
        "lateinit",
        "null",
        "object",
        "override",
        "package",
        "private",
        "public",
        "return",
        "sealed",
        "super",
        "suspend",
        "this",
        "throw",
        "true",
        "try",
        "typealias",
        "val",
        "var",
        "vararg",
        "when",
        "while",
        "operator",
        "inline",
        "reified",
        "out",
        "expect",
        "actual",
        "abstract",
        "open",
    )

/**
 * One run of source text, already HTML-escaped, and what it should be coloured as.
 *
 * The token list is matched in order and the first match wins, which is what keeps a keyword inside
 * a string from being coloured: the string rule is tried first and consumes the whole literal.
 */
private data class Rule(
    val cssClass: String,
    val pattern: Regex,
)

private val rules =
    listOf(
        Rule(cssClass = "tok-comment", pattern = Regex("""//[^\n]*|/\*[\s\S]*?\*/""")),
        Rule(cssClass = "tok-string", pattern = Regex(""""(?:[^"\\\n]|\\.)*"""")),
        Rule(cssClass = "tok-annotation", pattern = Regex("""@\w+""")),
        Rule(cssClass = "tok-number", pattern = Regex("""\b\d[\d_]*(?:\.\d+)?[fFLu]?\b""")),
        Rule(cssClass = "tok-keyword", pattern = Regex("""\b[a-zA-Z_]\w*\b""")),
    )

/**
 * Colours a Kotlin snippet that has **already been HTML-escaped**.
 *
 * Highlighting escaped text rather than raw source is deliberate. Escaping afterwards would have to
 * escape the markup this function just produced; escaping first means the only thing left to do is
 * wrap runs in spans, and no input can smuggle a tag through. The cost is that `&quot;` and `&amp;`
 * are several characters where the source had one, which the patterns account for by never trying to
 * match a bare quote.
 *
 * A single pass walks the text, so a keyword inside a string or a comment is never recoloured — the
 * earlier rule has already consumed it.
 */
fun highlightKotlin(escaped: String): String {
    val output = StringBuilder(escaped.length)
    var index = 0
    while (index < escaped.length) {
        val match = firstMatchAt(escaped = escaped, index = index)
        if (match == null) {
            output.append(escaped[index])
            index++
        } else {
            output.append(renderToken(rule = match.first, text = match.second))
            index += match.second.length
        }
    }
    return output.toString()
}

/** The first rule that matches exactly at [index], with the text it consumed. */
private fun firstMatchAt(
    escaped: String,
    index: Int,
): Pair<Rule, String>? =
    rules.firstNotNullOfOrNull { rule ->
        rule.pattern
            .matchAt(escaped, index)
            ?.value
            ?.takeIf { value -> value.isNotEmpty() }
            ?.let { value -> rule to value }
    }

/**
 * Wraps a token, except for identifiers that turned out not to be keywords.
 *
 * The keyword rule matches every word so that a word is consumed whole — otherwise `internal` would
 * match `in` and leave `ternal` behind to be matched again. Words that are not keywords are emitted
 * plain, and a capitalised one is treated as a type, which is close enough to right in Kotlin to be
 * worth the colour and never actively misleading.
 */
private fun renderToken(
    rule: Rule,
    text: String,
): String {
    val effectiveClass =
        when {
            rule.cssClass != "tok-keyword" -> rule.cssClass
            text in keywords -> "tok-keyword"
            text.first().isUpperCase() -> "tok-type"
            else -> ""
        }
    return if (effectiveClass.isEmpty()) {
        text
    } else {
        """<span class="$effectiveClass">$text</span>"""
    }
}
