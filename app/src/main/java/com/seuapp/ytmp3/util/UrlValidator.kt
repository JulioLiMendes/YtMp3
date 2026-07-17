package com.seuapp.ytmp3.util

object UrlValidator {

    private val REGEX_YOUTUBE = Regex(
        pattern = """^(https?://)?(www\.)?(youtube\.com/(watch\?v=|shorts/)|youtu\.be/)[\w-]+.*$""",
        option = RegexOption.IGNORE_CASE
    )

    fun ehLinkYoutubeValido(url: String): Boolean {
        return REGEX_YOUTUBE.matches(url.trim())
    }
}
