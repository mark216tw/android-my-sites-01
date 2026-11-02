package com.example.mysites.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SiteListQueryTest {
    private val sites = listOf(
        Site(
            id = "a",
            name = "Bravo",
            url = "https://bravo.example",
            createdAt = 1000L,
            favorite = true,
            lastUsedAt = 5000L,
            tagIds = listOf("work"),
        ),
        Site(
            id = "b",
            name = "alpha",
            url = "https://alpha.example",
            createdAt = 2000L,
            lastUsedAt = 0L,
            tagIds = listOf("fun"),
        ),
        Site(
            id = "c",
            name = "Charlie",
            url = "https://charlie.example",
            createdAt = 3000L,
            tagIds = listOf("work", "fun"),
        ),
    )

    @Test
    fun filterAllReturnsEverySite() {
        assertEquals(sites, filterSites(sites, "", SiteFilter.All))
    }

    @Test
    fun filterFavoritesReturnsOnlyFavorites() {
        val result = filterSites(sites, "", SiteFilter.Favorites)

        assertEquals(listOf("a"), result.map { it.id })
    }

    @Test
    fun filterByTagReturnsTaggedSites() {
        val result = filterSites(sites, "", SiteFilter.Tag("fun"))

        assertEquals(listOf("b", "c"), result.map { it.id })
    }

    @Test
    fun searchMatchesNameOrUrlAndCombinesWithFilter() {
        assertEquals(listOf("b"), filterSites(sites, "alpha", SiteFilter.All).map { it.id })
        assertEquals(listOf("c"), filterSites(sites, "charlie.example", SiteFilter.Tag("work")).map { it.id })
        assertEquals(emptyList<Site>(), filterSites(sites, "alpha", SiteFilter.Favorites))
    }

    @Test
    fun sortsByNameIgnoringCase() {
        assertEquals(
            listOf("b", "a", "c"),
            sortSites(sites, SiteSortMode.NAME_ASC).map { it.id },
        )
        assertEquals(
            listOf("c", "a", "b"),
            sortSites(sites, SiteSortMode.NAME_DESC).map { it.id },
        )
    }

    @Test
    fun sortsByRecentUseWithCreatedAtFallback() {
        assertEquals(
            listOf("a", "c", "b"),
            sortSites(sites, SiteSortMode.LAST_USED_DESC).map { it.id },
        )
        assertEquals(
            listOf("b", "c", "a"),
            sortSites(sites, SiteSortMode.LAST_USED_ASC).map { it.id },
        )
    }

    @Test
    fun sortsByCreatedTime() {
        assertEquals(
            listOf("c", "b", "a"),
            sortSites(sites, SiteSortMode.CREATED_DESC).map { it.id },
        )
        assertEquals(
            listOf("a", "b", "c"),
            sortSites(sites, SiteSortMode.CREATED_ASC).map { it.id },
        )
    }

    @Test
    fun customSortKeepsOriginalOrder() {
        assertEquals(sites, sortSites(sites, SiteSortMode.CUSTOM))
    }
}
