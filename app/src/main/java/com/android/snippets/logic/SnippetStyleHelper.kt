package com.android.snippets.logic

import com.android.snippets.viewmodel.SnippetStyle

object SnippetStyleHelper {

    val EMOTICONS_LIST: List<String> = listOf(
        ":)", ":(", ":D", ":P", ":O", ":-)", ":-(", ":-D", ":-P", ";)", ";D", 
        "^_^", ">_<", "o_o", "O_O", "-_-", "T_T", "8-)", "8)", ":3", "xD", "XD",
        ":' )", ":' (", ":v", ":/", ":\\", ">:)", ">:(", "O:)", "u_u"
    )

    fun containsEmoji(s: String): Boolean {
        var i = 0
        while (i < s.length) {
            val cp = s.codePointAt(i)
            if (cp in 0x1F300..0x1F9FF || cp in 0x1F600..0x1F64F || cp in 0x1F680..0x1F6FF || cp in 0x1F1E6..0x1F1FF) return true
            i += Character.charCount(cp)
        }
        return false
    }

    fun containsEmoticon(s: String): Boolean = EMOTICONS_LIST.any { s.contains(it) }

    fun getSnippetStyle(name: String): SnippetStyle {
        return SnippetStyle.Default
    }
}
