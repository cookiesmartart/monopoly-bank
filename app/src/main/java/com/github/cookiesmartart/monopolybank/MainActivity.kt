package com.github.cookiesmartart.monopolybank

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.cookiesmartart.monopolybank.data.settings.AppSettings
import com.github.cookiesmartart.monopolybank.data.settings.ThemeMode
import com.github.cookiesmartart.monopolybank.locale.LocalePrefs
import com.github.cookiesmartart.monopolybank.nfc.NfcController
import com.github.cookiesmartart.monopolybank.ui.about.AboutScreen
import com.github.cookiesmartart.monopolybank.ui.game.GameViewModel
import com.github.cookiesmartart.monopolybank.ui.history.HistoryScreen
import com.github.cookiesmartart.monopolybank.ui.home.HomeScreen
import com.github.cookiesmartart.monopolybank.ui.mainmenu.MainMenuScreen
import com.github.cookiesmartart.monopolybank.ui.nfc.LinkTagsScreen
import com.github.cookiesmartart.monopolybank.ui.settings.SettingsScreen
import com.github.cookiesmartart.monopolybank.ui.settings.SettingsViewModel
import com.github.cookiesmartart.monopolybank.ui.setup.SetupScreen
import com.github.cookiesmartart.monopolybank.ui.theme.MonopolyBankTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    // The in-app language override (set from Settings) is stored in plain, synchronous
    // SharedPreferences via LocalePrefs — not DataStore, and not AppCompatDelegate's per-app
    // language store — specifically because attachBaseContext runs before any coroutine could
    // complete and before any other Activity-independent locale mechanism is guaranteed to have
    // applied. Reading it here directly guarantees this activity always renders in the chosen
    // language, on every (re)creation, with no dependency on another library's internal state.
    override fun attachBaseContext(newBase: Context) {
        val tag = LocalePrefs.getLanguageTag(newBase)
        val wrapped = if (tag == null) {
            newBase
        } else {
            val configuration = Configuration(newBase.resources.configuration)
            configuration.setLocale(Locale.forLanguageTag(tag))
            newBase.createConfigurationContext(configuration)
        }
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MonopolyBankApplication
        val nfcController = NfcController(this)

        setContent {
            val settings by app.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MonopolyBankTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(app.repository))
                    val settingsViewModel: SettingsViewModel =
                        viewModel(factory = SettingsViewModel.factory(app.settingsRepository))
                    MonopolyBankRoot(viewModel, settingsViewModel, nfcController, settings)
                }
            }
        }
    }
}

@Composable
private fun MonopolyBankRoot(
    viewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    nfcController: NfcController,
    settings: AppSettings
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf<Screen>(Screen.MainMenu) }
    val nfcAvailable = nfcController.isHardwareAvailable && settings.nfcEnabled
    val allTagsLinked = uiState.players.isNotEmpty() && uiState.players.all { it.nfcTagId != null }

    if (uiState.isLoading) return

    when (val currentScreen = screen) {
        Screen.MainMenu -> MainMenuScreen(
            hasActiveGame = uiState.session != null,
            activePlayerCount = uiState.players.size,
            nfcHardwareAvailable = nfcController.isHardwareAvailable,
            onContinueGame = {
                screen = if (nfcAvailable && !allTagsLinked) Screen.LinkTags else Screen.Home
            },
            onStartNewGame = { nfcMode -> screen = Screen.Setup(nfcMode) }
        )

        is Screen.Setup -> {
            BackHandler { screen = Screen.MainMenu }
            SetupScreen(
                onStartGame = { names, colors, startingBalance ->
                    viewModel.startNewGame(names, colors, startingBalance)
                    settingsViewModel.setNfcEnabled(currentScreen.nfcMode)
                    screen = if (currentScreen.nfcMode) Screen.LinkTags else Screen.Home
                },
                defaultStartingBalance = settings.defaultStartingBalance
            )
        }

        Screen.LinkTags -> {
            BackHandler { screen = Screen.MainMenu }
            LinkTagsScreen(
                players = uiState.players,
                nfcController = nfcController,
                onLinkTag = { playerId, tagId -> viewModel.linkNfcTag(playerId, tagId) },
                soundEnabled = settings.soundEnabled,
                vibrationEnabled = settings.vibrationEnabled,
                onContinue = { screen = Screen.Home }
            )
        }

        Screen.Home -> {
            BackHandler { screen = Screen.MainMenu }
            HomeScreen(
                players = uiState.players,
                onPayToBank = { playerId, amount -> viewModel.payToBank(playerId, amount) },
                onReceiveFromBank = { playerId, amount -> viewModel.receiveFromBank(playerId, amount) },
                onTransfer = { fromId, toId, amount -> viewModel.transfer(fromId, toId, amount) },
                onNewGame = {
                    viewModel.endActiveGame()
                    screen = Screen.MainMenu
                },
                onOpenHistory = { screen = Screen.History },
                nfcAvailable = nfcAvailable,
                nfcController = nfcController,
                onLinkTag = { playerId, tagId -> viewModel.linkNfcTag(playerId, tagId) },
                onMarkBankrupt = { playerId -> viewModel.markBankrupt(playerId) },
                soundEnabled = settings.soundEnabled,
                vibrationEnabled = settings.vibrationEnabled,
                onOpenSettings = { screen = Screen.Settings }
            )
        }

        Screen.History -> {
            BackHandler { screen = Screen.Home }
            HistoryScreen(
                players = uiState.players,
                transactions = uiState.transactions,
                onBack = { screen = Screen.Home }
            )
        }

        Screen.Settings -> {
            BackHandler { screen = Screen.Home }
            SettingsScreen(
                settings = settings,
                nfcHardwareAvailable = nfcController.isHardwareAvailable,
                onThemeModeChange = { settingsViewModel.setThemeMode(it) },
                onSoundChange = { settingsViewModel.setSoundEnabled(it) },
                onVibrationChange = { settingsViewModel.setVibrationEnabled(it) },
                onNfcChange = { settingsViewModel.setNfcEnabled(it) },
                onDefaultStartingBalanceChange = { settingsViewModel.setDefaultStartingBalance(it) },
                onOpenAbout = { screen = Screen.About },
                onBack = { screen = Screen.Home }
            )
        }

        Screen.About -> {
            BackHandler { screen = Screen.Settings }
            AboutScreen(onBack = { screen = Screen.Settings })
        }
    }
}
