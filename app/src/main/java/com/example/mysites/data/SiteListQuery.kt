package com.example.mysites.data

sealed interface SiteFilter {
    data object All : SiteFilter
    data object Favorites : SiteFilter
    data class Tag(val tagId: String) : SiteFilter
}

fun filterSites(sites: List<Site>, query: String, filter: SiteFilter): List<Site> {
    val trimmedQuery = query.trim()
    val byFilter = when (filter) {
        SiteFilter.All -> sites
        SiteFilter.Favorites -> sites.filter { it.favorite }
        is SiteFilter.Tag -> sites.filter { filter.tagId in it.tagIds }
    }
    if (trimmedQuery.isEmpty()) return byFilter

    return byFilter.filter { site ->
        site.name.contains(trimmedQuery, ignoreCase = true) ||
            site.url.contains(trimmedQuery, ignoreCase = true)
    }
}

fun sortSites(sites: List<Site>, mode: SiteSortMode): List<Site> {
    return when (mode) {
        SiteSortMode.CUSTOM -> sites
        SiteSortMode.NAME_ASC -> sites.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.name },
        )
        SiteSortMode.NAME_DESC -> sites.sortedWith(
            compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name },
        )
        SiteSortMode.LAST_USED_DESC -> sites.sortedWith(
            compareByDescending<Site> { it.recencyValue() }.thenByDescending { it.createdAt },
        )
        SiteSortMode.LAST_USED_ASC -> sites.sortedWith(
            compareBy<Site> { it.recencyValue() }.thenBy { it.createdAt },
        )
        SiteSortMode.CREATED_DESC -> sites.sortedWith(
            compareByDescending<Site> { it.createdAt }.thenByDescending { it.id },
        )
        SiteSortMode.CREATED_ASC -> sites.sortedWith(
            compareBy<Site> { it.createdAt }.thenBy { it.id },
        )
    }
}

private fun Site.recencyValue(): Long = if (lastUsedAt > 0L) lastUsedAt else createdAt
