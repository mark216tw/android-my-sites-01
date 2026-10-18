package com.example.mysites.data

import java.net.IDN
import java.net.URI

object UrlNormalizer {
    private val explicitSchemeRegex = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")
    private val schemeWithColonRegex = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.any { it.isWhitespace() }) return null

        val candidate = when {
            explicitSchemeRegex.containsMatchIn(trimmed) -> trimmed

            schemeWithColonRegex.containsMatchIn(trimmed) -> {
                val prefix = trimmed.substringBefore(':')
                val remainder = trimmed.substringAfter(':')
                val looksLikeHostAndPort =
                    prefix.contains('.') || remainder.firstOrNull()?.isDigit() == true
                if (looksLikeHostAndPort) "https://$trimmed" else return null
            }

            else -> "https://$trimmed"
        }

        val scheme = candidate.substringBefore("://").lowercase()
        if (scheme != "http" && scheme != "https") return null
        if (candidate.substringAfter("://").isBlank()) return null

        return if (hasValidHost(candidate)) candidate else null
    }

    private fun hasValidHost(candidate: String): Boolean {
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return false
        val host = uri.host
        if (!host.isNullOrBlank()) return true

        val authority = uri.rawAuthority ?: return false
        val hostPart = authority.substringAfterLast('@').substringBefore(':')
        if (hostPart.isBlank()) return false
        return runCatching { IDN.toASCII(hostPart).isNotBlank() }.getOrDefault(false)
    }
}
