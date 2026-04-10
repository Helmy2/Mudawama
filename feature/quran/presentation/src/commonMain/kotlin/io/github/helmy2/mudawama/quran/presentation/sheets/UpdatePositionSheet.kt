package io.github.helmy2.mudawama.quran.presentation.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import io.github.helmy2.mudawama.designsystem.components.MudawamaBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.quran.domain.model.ALL_SURAHS
import io.github.helmy2.mudawama.quran.domain.model.SurahMetadata
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_position_of_ayah_format
import mudawama.shared.designsystem.quran_position_sheet_done_button
import mudawama.shared.designsystem.quran_position_sheet_search_hint
import mudawama.shared.designsystem.quran_position_sheet_title
import mudawama.shared.designsystem.quran_position_sheet_verse_label
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePositionSheet(
    initialSurah: Int = 1,
    initialAyah: Int = 1,
    onDone: (surah: Int, ayah: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSurah by rememberSaveable { mutableIntStateOf(initialSurah) }
    var selectedAyah by rememberSaveable { mutableIntStateOf(initialAyah) }

    val filteredSurahs = if (searchQuery.isBlank()) {
        ALL_SURAHS
    } else {
        ALL_SURAHS.filter { it.nameEn.contains(searchQuery, ignoreCase = true) }
    }

    val currentSurahMeta = ALL_SURAHS.getOrNull(selectedSurah - 1) ?: ALL_SURAHS.first()

    MudawamaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .height(600.dp),
        ) {
            Text(
                text = stringResource(Res.string.quran_position_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.quran_position_sheet_search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* dismiss keyboard */ }),
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Surah list
                LazyColumn(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight(),
                ) {
                    itemsIndexed(filteredSurahs) { _, surah ->
                        SurahItem(
                            surah = surah,
                            isSelected = surah.number == selectedSurah,
                            onClick = {
                                selectedSurah = surah.number
                                // Clamp ayah to new Surah max
                                selectedAyah = selectedAyah.coerceAtMost(surah.ayahCount)
                            },
                        )
                    }
                }

                // Ayah picker
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    Text(
                        text = stringResource(Res.string.quran_position_sheet_verse_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    val ayahListState = rememberLazyListState()
                    LazyColumn(
                        state = ayahListState,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        items(currentSurahMeta.ayahCount) { index ->
                            val ayah = index + 1
                            val isAyahSelected = ayah == selectedAyah
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isAyahSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { selectedAyah = ayah }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = ayah.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isAyahSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAyahSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(Res.string.quran_position_of_ayah_format, currentSurahMeta.ayahCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onDone(selectedSurah, selectedAyah) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.quran_position_sheet_done_button))
            }
        }
    }
}

@Composable
private fun SurahItem(
    surah: SurahMetadata,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceContainerLow
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${surah.number}. ${surah.nameEn}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview
@Composable
private fun UpdatePositionSheetPreview() {
    UpdatePositionSheet(
        onDone = { _, _ -> },
        onDismiss = {},
    )
}
