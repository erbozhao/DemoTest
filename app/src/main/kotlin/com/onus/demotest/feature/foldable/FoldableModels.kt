package com.onus.demotest.feature.foldable

data class FoldableItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val body: String,
)

enum class FoldableDemoType(val title: String) {
    LIST_DETAIL("ListDetail"),
    FEED("Feed"),
    FILES("Files"),
    SUPPORTING_PANE("SupportingPane"),
    ADAPTIVE_NAVIGATION("Navigation"),
    ACTIVITY_EMBEDDING("ActivityEmbedding"),
}

interface FoldableBackHandler {
    fun handleBack(): Boolean
}

internal fun sampleMails(): List<FoldableItem> = listOf(
    FoldableItem(1, "Foldable layout review", "Design Team", "Use a compact single-pane list for portrait phones. On medium and expanded widths, keep the list visible and render the selected message in the detail pane."),
    FoldableItem(2, "Canonical layout guidance", "Android Docs", "Canonical layouts help apps adapt across phones, tablets, Chromebooks, and foldables. The list-detail pattern fits collection browsing."),
    FoldableItem(3, "Portrait behavior", "QA", "In compact width, the detail pane should be reached after selecting a row. Back navigation returns to the list."),
    FoldableItem(4, "Unfolded behavior", "Product", "When the app has medium or expanded width, initialize with list and detail visible so horizontal space is useful."),
)

internal fun sampleCards(): List<FoldableItem> = listOf(
    FoldableItem(1, "Compact", "1 column", "Feed content uses one column on narrow screens."),
    FoldableItem(2, "Medium", "2 columns", "Medium width keeps scanning efficient with two columns."),
    FoldableItem(3, "Expanded", "3 columns", "Large windows can expose more feed items without increasing scroll depth."),
    FoldableItem(4, "Foldable", "adaptive", "Window changes update the grid without changing the content model."),
    FoldableItem(5, "Review", "stable state", "Selected content should remain stable while layout adapts."),
)

internal fun sampleFiles(): List<FoldableItem> = listOf(
    FoldableItem(1, "Documents", "12 files", "Project briefs, reports, and specs."),
    FoldableItem(2, "Images", "34 files", "Screenshots and design references."),
    FoldableItem(3, "Downloads", "8 files", "Recently downloaded files."),
    FoldableItem(4, "Shared", "5 files", "Files shared with the team."),
)
