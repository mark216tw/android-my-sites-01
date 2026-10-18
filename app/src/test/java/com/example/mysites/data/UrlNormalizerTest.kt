package com.example.mysites.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlNormalizerTest {
    @Test
    fun addsHttpsWhenSchemeIsMissing() {
        assertEquals("https://example.com", UrlNormalizer.normalize("example.com"))
    }

    @Test
    fun keepsExistingScheme() {
        assertEquals("http://example.com", UrlNormalizer.normalize("http://example.com"))
        assertEquals("https://example.com/path?a=1", UrlNormalizer.normalize("https://example.com/path?a=1"))
    }

    @Test
    fun trimsSurroundingWhitespace() {
        assertEquals("https://example.com", UrlNormalizer.normalize("  example.com  "))
    }

    @Test
    fun acceptsLocalhostAndPorts() {
        assertEquals("https://localhost:8080", UrlNormalizer.normalize("localhost:8080"))
        assertEquals("https://example.com:8080", UrlNormalizer.normalize("example.com:8080"))
        assertEquals("https://192.168.1.1", UrlNormalizer.normalize("192.168.1.1"))
    }

    @Test
    fun acceptsUnicodeHost() {
        assertEquals("https://例子.測試", UrlNormalizer.normalize("例子.測試"))
    }

    @Test
    fun rejectsBlankInput() {
        assertNull(UrlNormalizer.normalize(""))
        assertNull(UrlNormalizer.normalize("   "))
    }

    @Test
    fun rejectsUnsupportedSchemes() {
        assertNull(UrlNormalizer.normalize("ftp://example.com"))
        assertNull(UrlNormalizer.normalize("javascript:alert(1)"))
        assertNull(UrlNormalizer.normalize("mailto:test@example.com"))
    }

    @Test
    fun rejectsMissingHost() {
        assertNull(UrlNormalizer.normalize("https://"))
        assertNull(UrlNormalizer.normalize("https:///path"))
    }

    @Test
    fun rejectsUrlsWithSpaces() {
        assertNull(UrlNormalizer.normalize("https://example.com/a b"))
    }
}
