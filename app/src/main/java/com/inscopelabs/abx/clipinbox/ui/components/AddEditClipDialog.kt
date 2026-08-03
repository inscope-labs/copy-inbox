package com.inscopelabs.abx.clipinbox.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inscopelabs.abx.clipinbox.data.model.ClipItem
import com.inscopelabs.abx.clipinbox.ui.theme.CategoryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClipDialog(
    clipToEdit: ClipItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, colorTagIndex: Int, id: Long) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(clipToEdit?.title ?: "") }
    var content by remember { mutableStateOf(clipToEdit?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(clipToEdit?.category ?: "Notes") }
    var selectedColorIndex by remember { mutableIntStateOf(clipToEdit?.colorTagIndex ?: 0) }

    val isEditing = clipToEdit != null
    val categories = listOf("Notes", "Code", "Link", "Work", "Personal")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("add_edit_clip_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Snippet" else "Add New Snippet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Paste from clipboard button
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        if (clipboard.hasPrimaryClip()) {
                            val clipData = clipboard.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pasted = clipData.getItemAt(0).text?.toString() ?: ""
                                if (pasted.isNotBlank()) {
                                    content = pasted
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("dialog_paste_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Paste Clipboard")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (Optional)") },
                placeholder = { Text("e.g. Wi-Fi Password, Code Snippet") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clip_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Snippet Content *") },
                placeholder = { Text("Type or paste your text snippet here...") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clip_content_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("dialog_category_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Tag Accent
            Text(
                text = "Color Tag Accent",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(CategoryColors) { index, color ->
                    val isSelected = index == selectedColorIndex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                } else Modifier
                            )
                            .clickable { selectedColorIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    enabled = content.isNotBlank(),
                    onClick = {
                        onSave(
                            title,
                            content,
                            selectedCategory,
                            selectedColorIndex,
                            clipToEdit?.id ?: 0L
                        )
                        onDismiss()
                    },
                    modifier = Modifier.testTag("dialog_save_clip_button")
                ) {
                    Text(if (isEditing) "Save Changes" else "Add to Inbox")
                }
            }
        }
    }
}
