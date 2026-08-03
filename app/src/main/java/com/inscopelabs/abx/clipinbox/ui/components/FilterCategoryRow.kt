package com.inscopelabs.abx.clipinbox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class CategoryFilter(
    val name: String,
    val icon: ImageVector
)

val AvailableCategories = listOf(
    CategoryFilter("All", Icons.Default.Folder),
    CategoryFilter("Pinned", Icons.Default.PushPin),
    CategoryFilter("Notes", Icons.Default.Description),
    CategoryFilter("Code", Icons.Default.Code),
    CategoryFilter("Link", Icons.Default.Link),
    CategoryFilter("Work", Icons.Default.Work),
    CategoryFilter("Personal", Icons.Default.Person)
)

@Composable
fun FilterCategoryRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AvailableCategories) { category ->
            val isSelected = category.name == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category.name) },
                label = { Text(category.name) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
            )
        }
    }
}
