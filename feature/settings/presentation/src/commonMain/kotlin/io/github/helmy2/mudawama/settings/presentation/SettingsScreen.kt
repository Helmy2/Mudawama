package io.github.helmy2.mudawama.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.designsystem.components.MudawamaTopAppBar
import io.github.helmy2.mudawama.settings.domain.AppLanguage
import io.github.helmy2.mudawama.settings.domain.AppTheme
import io.github.helmy2.mudawama.settings.domain.CalculationMethod
import io.github.helmy2.mudawama.settings.domain.LocationMode
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.action_save
import mudawama.shared.designsystem.settings_label_latitude
import mudawama.shared.designsystem.settings_label_longitude
import mudawama.shared.designsystem.settings_method_dubai
import mudawama.shared.designsystem.settings_method_egyptian
import mudawama.shared.designsystem.settings_method_isna
import mudawama.shared.designsystem.settings_method_karachi
import mudawama.shared.designsystem.settings_method_kuwait
import mudawama.shared.designsystem.settings_method_msc
import mudawama.shared.designsystem.settings_method_mwl
import mudawama.shared.designsystem.settings_method_qatar
import mudawama.shared.designsystem.settings_method_singapore
import mudawama.shared.designsystem.settings_method_tehran
import mudawama.shared.designsystem.settings_method_turkey
import mudawama.shared.designsystem.settings_method_umm_alqura
import mudawama.shared.designsystem.settings_notification_evening
import mudawama.shared.designsystem.settings_notification_morning
import mudawama.shared.designsystem.settings_option_gps_automatic
import mudawama.shared.designsystem.settings_option_language_arabic
import mudawama.shared.designsystem.settings_option_language_english
import mudawama.shared.designsystem.settings_option_manual
import mudawama.shared.designsystem.settings_option_theme_dark
import mudawama.shared.designsystem.settings_option_theme_light
import mudawama.shared.designsystem.settings_option_theme_system
import mudawama.shared.designsystem.settings_placeholder_title
import mudawama.shared.designsystem.settings_section_appearance
import mudawama.shared.designsystem.settings_section_calculation_method
import mudawama.shared.designsystem.settings_section_language
import mudawama.shared.designsystem.settings_section_location
import mudawama.shared.designsystem.settings_section_notifications
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showMethodPicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MudawamaTopAppBar(
                title = { Text(stringResource(Res.string.settings_placeholder_title)) },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack,
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                Text("Loading...")
            } else {
                // Calculation Method Section
                SettingsSection(title = stringResource(Res.string.settings_section_calculation_method)) {
                    Text(
                        text = getMethodDisplayName(state.settings.calculationMethod),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMethodPicker = true }
                            .padding(8.dp)
                    )
                }

                // Location Section
                SettingsSection(title = stringResource(Res.string.settings_section_location)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.settings.locationMode is LocationMode.Gps,
                                onClick = {
                                    viewModel.onAction(
                                        SettingsAction.SetLocationMode(
                                            LocationMode.Gps
                                        )
                                    )
                                }
                            )
                            Text(stringResource(Res.string.settings_option_gps_automatic))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.settings.locationMode is LocationMode.Manual,
                                onClick = {
                                    val lat = state.latitudeInput.toDoubleOrNull() ?: 21.3891
                                    val lon = state.longitudeInput.toDoubleOrNull() ?: 39.8579
                                    viewModel.onAction(
                                        SettingsAction.SetLocationMode(
                                            LocationMode.Manual(
                                                lat,
                                                lon
                                            )
                                        )
                                    )
                                }
                            )
                            Text(stringResource(Res.string.settings_option_manual))
                        }
                    }

                    if (state.settings.locationMode is LocationMode.Manual) {
                        OutlinedTextField(
                            value = state.latitudeInput,
                            onValueChange = {
                                viewModel.onAction(
                                    SettingsAction.UpdateLatitudeInput(
                                        it
                                    )
                                )
                            },
                            label = { Text(stringResource(Res.string.settings_label_latitude)) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.latitudeError != null,
                            supportingText = state.latitudeError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.longitudeInput,
                            onValueChange = {
                                viewModel.onAction(
                                    SettingsAction.UpdateLongitudeInput(
                                        it
                                    )
                                )
                            },
                            label = { Text(stringResource(Res.string.settings_label_longitude)) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.longitudeError != null,
                            supportingText = state.longitudeError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.onAction(SettingsAction.SaveManualLocation) }
                        ) {
                            Text(stringResource(Res.string.action_save))
                        }
                    }
                }

                // Theme Section
                SettingsSection(title = stringResource(Res.string.settings_section_appearance)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AppTheme.entries.forEach { theme ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.onAction(SettingsAction.SetAppTheme(theme))
                                }
                            ) {
                                RadioButton(
                                    selected = state.settings.appTheme == theme,
                                    onClick = { viewModel.onAction(SettingsAction.SetAppTheme(theme)) }
                                )
                                Text(getThemeDisplayName(theme))
                            }
                        }
                    }
                }

                // Language Section
                SettingsSection(title = stringResource(Res.string.settings_section_language)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AppLanguage.entries.forEach { language ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.onAction(SettingsAction.SetAppLanguage(language))
                                }
                            ) {
                                RadioButton(
                                    selected = state.settings.appLanguage == language,
                                    onClick = {
                                        viewModel.onAction(
                                            SettingsAction.SetAppLanguage(
                                                language
                                            )
                                        )
                                    }
                                )
                                Text(getLanguageDisplayName(language))
                            }
                        }
                    }
                }

                // Notification Section
                SettingsSection(title = stringResource(Res.string.settings_section_notifications)) {
                    NotificationToggleRow(
                        label = stringResource(Res.string.settings_notification_morning),
                        enabled = state.morningNotificationEnabled,
                        onToggle = { viewModel.onAction(SettingsAction.SetMorningNotification(it)) }
                    )
                    NotificationToggleRow(
                        label = stringResource(Res.string.settings_notification_evening),
                        enabled = state.eveningNotificationEnabled,
                        onToggle = { viewModel.onAction(SettingsAction.SetEveningNotification(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    MudawamaSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}


@Composable
private fun getMethodDisplayName(method: CalculationMethod): String {
    return when (method) {
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> stringResource(Res.string.settings_method_mwl)
        CalculationMethod.EGYPTIAN -> stringResource(Res.string.settings_method_egyptian)
        CalculationMethod.UMM_AL_QURA -> stringResource(Res.string.settings_method_umm_alqura)
        CalculationMethod.KARACHI -> stringResource(Res.string.settings_method_karachi)
        CalculationMethod.ISNA -> stringResource(Res.string.settings_method_isna)
        CalculationMethod.DUBAI -> stringResource(Res.string.settings_method_dubai)
        CalculationMethod.KUWAIT -> stringResource(Res.string.settings_method_kuwait)
        CalculationMethod.QATAR -> stringResource(Res.string.settings_method_qatar)
        CalculationMethod.MOON_SIGHTING_COMMITTEE -> stringResource(Res.string.settings_method_msc)
        CalculationMethod.SINGAPORE -> stringResource(Res.string.settings_method_singapore)
        CalculationMethod.TURKEY -> stringResource(Res.string.settings_method_turkey)
        CalculationMethod.TEHRAN -> stringResource(Res.string.settings_method_tehran)
    }
}

@Composable
private fun getThemeDisplayName(theme: AppTheme): String {
    return when (theme) {
        AppTheme.SYSTEM -> stringResource(Res.string.settings_option_theme_system)
        AppTheme.LIGHT -> stringResource(Res.string.settings_option_theme_light)
        AppTheme.DARK -> stringResource(Res.string.settings_option_theme_dark)
    }
}

@Composable
private fun getLanguageDisplayName(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> stringResource(Res.string.settings_option_language_english)
        AppLanguage.ARABIC -> stringResource(Res.string.settings_option_language_arabic)
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
        )
    }
}