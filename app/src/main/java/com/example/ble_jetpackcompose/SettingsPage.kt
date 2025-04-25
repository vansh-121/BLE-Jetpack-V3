package com.example.ble_jetpackcompose

import android.app.Dialog
import android.content.Intent
//import com.example.ble_jetpackcompose.BuildConfig
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.net.URL
import java.net.URLEncoder
import kotlin.random.Random

// Create a singleton object to manage app-wide theme state
object ThemeManager {
    private val _isDarkMode = MutableStateFlow(false) // Default to false until initialized
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private var isInitialized = false

    fun toggleDarkMode(value: Boolean) {
        _isDarkMode.value = value
        isInitialized = true // Mark as initialized once toggled
    }

    fun initializeWithSystemTheme(isSystemDark: Boolean) {
        if (!isInitialized) {
            _isDarkMode.value = isSystemDark
            isInitialized = true
        }
    }
}

// Create a singleton object to manage language state
object LanguageManager {
    // Add the missing private mutable state flow
    val _currentLanguage = MutableStateFlow(java.util.Locale.ENGLISH.language)
    val currentLanguage: StateFlow<String> = _currentLanguage

    // List of supported Indian languages
    val supportedLanguages = listOf(
        LanguageOption("en", "English"),
        LanguageOption("hi", "हिन्दी (Hindi)"),
        LanguageOption("ta", "தமிழ் (Tamil)"),
        LanguageOption("te", "తెలుగు (Telugu)"),
        LanguageOption("bn", "বাংলা (Bengali)"),
        LanguageOption("mr", "मराठी (Marathi)"),
        LanguageOption("gu", "ગુજરાતી (Gujarati)"),
        LanguageOption("kn", "ಕನ್ನಡ (Kannada)"),
        LanguageOption("ml", "മലയാളം (Malayalam)"),
        LanguageOption("pa", "ਪੰਜਾਬੀ (Punjabi)"),
        LanguageOption("or", "ଓଡ଼ିଆ (Odia)")
    )

    fun setLanguage(languageCode: String) {
        _currentLanguage.value = languageCode
    }

    fun getLanguageName(languageCode: String): String {
        return supportedLanguages.find { it.code == languageCode }?.name ?: "English"
    }
}
//interface TranslationService {
//    suspend fun translateText(text: String, targetLanguage: String): String
//}
interface GeminiTranslationApi {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun translateText(
        @Body request: TranslationRequest,
        @Query("key") apiKey: String
    ): TranslationResponse
}

// Update TranslationRequest and Response models
data class TranslationRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f
)

data class TranslationResponse(
    val candidates: List<TranslationCandidate>
)

data class TranslationCandidate(
    val content: Content
)
interface TranslationService {
    suspend fun translateText(text: String, targetLanguage: String): String
}
object TranslationCache {
    private val cache = mutableMapOf<String, String>()


    fun get(key: String): String? = cache[key]
    fun put(key: String, value: String) {
        cache[key] = value
    }
}


// Modified GoogleTranslationService
class GoogleTranslationService(
    private val apiKey: String = com.example.ble_jetpackcompose.BuildConfig.API_KEY
) : TranslationService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val translationApi = retrofit.create(GeminiTranslationApi::class.java)

    override suspend fun translateText(
        text: String,
        targetLanguage: String
    ): String = translateBatch(listOf(text), targetLanguage).first()

    // Batch translation method
    suspend fun translateBatch(
        texts: List<String>,
        targetLanguage: String
    ): List<String> {
        return try {
            // Check cache first
            val uncachedTexts = texts.filter { text ->
                TranslationCache.get("$text-$targetLanguage") == null
            }

            if (uncachedTexts.isEmpty()) {
                return texts.map { TranslationCache.get("$it-$targetLanguage")!! }
            }

            val prompt = """
                Provide only the translations of the following texts into ${getLanguageName(targetLanguage)}, 
                one per line, in the same order, with no additional text or explanations:
                ${texts.joinToString("\n")}
            """.trimIndent()

            val request = TranslationRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(prompt))
                    )
                ),
                generationConfig = GenerationConfig()
            )

            val response = translationApi.translateText(request, apiKey)
            val translatedText = response.candidates.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?.trim() ?: texts.joinToString("\n")

            val translatedList = translatedText.split("\n")
            // Cache results
            texts.forEachIndexed { index, original ->
                val translated = translatedList.getOrElse(index) { original }
                TranslationCache.put("$original-$targetLanguage", translated)
            }

            // Return translated texts
            texts.map { TranslationCache.get("$it-$targetLanguage") ?: it }
        } catch (e: Exception) {
            e.printStackTrace()
            texts // Return original texts if translation fails
        }
    }

    private fun getLanguageName(languageCode: String): String {
        // Map language codes to their names
        return when (languageCode) {
            "en" -> "English"
            "hi" -> "Hindi"
            "ta" -> "Tamil"
            "te" -> "Telugu"
            "bn" -> "Bengali"
            "mr" -> "Marathi"
            "gu" -> "Gujarati"
            "kn" -> "Kannada"
            "ml" -> "Malayalam"
            "pa" -> "Punjabi"
            "or" -> "Odia"
            else -> languageCode
        }
    }
}

//// Extension function for easy translation
//suspend fun String.translateTo(
//    targetLanguage: String,
//    apiKey: String = "AIzaSyBr_EdKrLRXftUK9MN2TDTKctiEZD6-mOM"
//): String {
//    val translator = GoogleTranslationService(apiKey)
//    return translator.translateText(this, targetLanguage)
//}

// Data class for language options
data class LanguageOption(val code: String, val name: String)

data class TranslatedSettings(
    val settingsTitle: String = "Settings",
    val darkMode: String = "Dark Mode",
    val language: String = "Language",
    val help: String = "Help",
    val accounts: String = "Accounts",
    val about: String = "About BLE"
)

// Update ModernSettingsScreen
@Composable
fun ModernSettingsScreen(
    viewModel: AuthViewModel = viewModel(),
    onSignOut: () -> Unit,
    navController: NavHostController
) {
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // State for translated text with initial fallback
    var translatedText by remember {
        mutableStateOf(
            TranslatedSettings(
                settingsTitle = TranslationCache.get("Settings-$currentLanguage") ?: "Settings",
                darkMode = TranslationCache.get("Dark Mode-$currentLanguage") ?: "Dark Mode",
                language = TranslationCache.get("Language-$currentLanguage") ?: "Language",
                help = TranslationCache.get("Help-$currentLanguage") ?: "Help",
                accounts = TranslationCache.get("Accounts-$currentLanguage") ?: "Accounts",
                about = TranslationCache.get("About BLE-$currentLanguage") ?: "About BLE"
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf(
            "Settings", "Dark Mode", "Language", "Help", "Accounts", "About BLE"
        )
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedSettings(
            settingsTitle = translatedList[0],
            darkMode = translatedList[1],
            language = translatedList[2],
            help = translatedList[3],
            accounts = translatedList[4],
            about = translatedList[5]
        )
    }

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF2F2F7)
    val cardBackground = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
    val dividerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)
    val iconTint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF)

    val currentUser = viewModel.checkCurrentUser()

    Scaffold(
        backgroundColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = translatedText.settingsTitle,
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                backgroundColor = backgroundColor,
                elevation = 0.dp
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                isDarkMode = isDarkMode
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            UserProfileCard(
                cardBackground = cardBackground,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                iconTint = iconTint,
                userName = when {
                    currentUser?.isAnonymous == true -> "Guest User"
                    currentUser != null -> currentUser.email?.substringBefore('@') ?: "User"
                    else -> "Not Signed In"
                },
                userEmail = when {
                    currentUser?.isAnonymous == true -> "Anonymous User"
                    currentUser != null -> currentUser.email ?: ""
                    else -> ""
                },
                profilePictureUrl = currentUser?.photoUrl?.toString(), // Pass profile picture URL
                onLogout = {
                    viewModel.signOut()
                    onSignOut()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsOptionsList(
                cardBackground = cardBackground,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                dividerColor = dividerColor,
                iconTint = iconTint,
                isDarkMode = isDarkMode,
                translatedText = translatedText,
                onDarkModeToggle = { newValue ->
                    ThemeManager.toggleDarkMode(newValue)
                },
                navController = navController
            )
        }
    }
}
//// Helper function to generate a random color for the avatar (fallback)
//fun generateRandomColor(): Color {
//    val random = Random.Default
//    return Color(
//        red = random.nextInt(256),
//        green = random.nextInt(256),
//        blue = random.nextInt(256)
//    ).copy(alpha = 0.8f)


@Composable
fun UserProfileCard(
    cardBackground: Color,
    textColor: Color,
    secondaryTextColor: Color,
    iconTint: Color,
    userName: String,
    userEmail: String,
    profilePictureUrl: String? = null, // Add profile picture URL parameter
    onLogout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardBackground,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image (Google picture or fallback)
            if (profilePictureUrl != null) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
//                    placeholder = painterResource(R.drawable.placeholder), // Optional
                    error = painterResource(R.drawable.error), // Optional
                    contentScale = ContentScale.Crop
                )
            } else {
                val avatarColor = remember { generateRandomColor() }
                val initials = userName.take(2).uppercase()

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(avatarColor),

                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userName,
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                Text(
                    text = userEmail,
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.body2.copy(
                        color = secondaryTextColor
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = iconTint
                )
            }
        }
    }
}

//// Helper function to generate a random color for the avatar (fallback)
//fun generateRandomColor(): Color {
//    val random = Random.Default
//    return Color(
//        red = random.nextInt(256),
//        green = random.nextInt(256),
//        blue = random.nextInt(256)
//    ).copy(alpha = 0.8f)
//}
@Composable
fun SettingsOptionsList(
    cardBackground: Color,
    textColor: Color,
    secondaryTextColor: Color,
    dividerColor: Color,
    iconTint: Color,
    isDarkMode: Boolean,
    translatedText: TranslatedSettings,
    onDarkModeToggle: (Boolean) -> Unit,
    navController: NavHostController
) {
    val settingsOptions = listOf(
        SettingsItem(Icons.Outlined.DarkMode, translatedText.darkMode, SettingsItemType.SWITCH),
        SettingsItem(Icons.Outlined.Language, translatedText.language, SettingsItemType.DETAIL),
        SettingsItem(Icons.AutoMirrored.Outlined.Help, translatedText.help, SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.AccountCircle, translatedText.accounts, SettingsItemType.DETAIL),
        SettingsItem(Icons.Outlined.Info, translatedText.about, SettingsItemType.DETAIL)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardBackground,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            settingsOptions.forEachIndexed { index, item ->
                if (item.title == translatedText.darkMode) {
                    SettingsItemRow(
                        item = item,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        iconTint = iconTint,
                        initialSwitchState = isDarkMode,
                        onSwitchChange = onDarkModeToggle,
                        navController = navController
                    )
                } else {
                    SettingsItemRow(
                        item = item,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        iconTint = iconTint,
                        initialSwitchState = isDarkMode,
                        navController = navController
                    )
                }

                if (index < settingsOptions.size - 1) {
                    Divider(
                        color = dividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
// Modify your SettingsItemRow function to handle language selection
// Update SettingsItemRow with better dialog state management
@Composable
fun SettingsItemRow(
    item: SettingsItem,
    textColor: Color,
    secondaryTextColor: Color,
    iconTint: Color,
    initialSwitchState: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    navController: NavHostController? = null
) {
    var switchState by remember { mutableStateOf(initialSwitchState) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAccountsDialog by remember { mutableStateOf(false) }
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val context = LocalContext.current
    val isLanguageItem = item.icon == Icons.Outlined.Language
    val isAboutItem = item.icon == Icons.Outlined.Info
    val isHelpItem = item.icon == Icons.AutoMirrored.Outlined.Help
    val isAccountsItem = item.icon == Icons.Outlined.AccountCircle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.type == SettingsItemType.DETAIL) {
                when {
                    isLanguageItem -> showLanguageDialog = true
                    isAboutItem -> showAboutDialog = true
                    isHelpItem -> showHelpDialog = true
                    isAccountsItem -> showAccountsDialog = true
                }
            }
            .padding(16.dp), // Restored original padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title,
            fontFamily = helveticaFont,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Medium,
                color = textColor
            ),
            modifier = Modifier.weight(1f) // Restored alignment with weight
        )

        when (item.type) {
            SettingsItemType.SWITCH -> {
                Switch(
                    checked = switchState,
                    onCheckedChange = { newValue ->
                        switchState = newValue
                        onSwitchChange?.invoke(newValue)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = iconTint,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = secondaryTextColor.copy(alpha = 0.3f)
                    )
                )
            }
            SettingsItemType.DETAIL -> {
                if (isLanguageItem) {
                    Text(
                        text = LanguageManager.getLanguageName(currentLanguage),
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.body2.copy(
                            color = secondaryTextColor
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Navigate",
                    tint = secondaryTextColor
                )
            }
        }
    }

    // Language selection dialog (unchanged)
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = "Select Language",
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                LazyColumn {
                    items(LanguageManager.supportedLanguages) { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    LanguageManager.setLanguage(language.code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language.name,
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )
                            if (language.code == currentLanguage) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = iconTint
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            },
            backgroundColor = if (initialSwitchState) Color(0xFF1E1E1E) else Color.White,
            contentColor = if (initialSwitchState) Color.White else Color.Black,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .heightIn(max = 500.dp)
        )
    }

    // About BLE dialog (unchanged)
    if (showAboutDialog) {
        Dialog(
            onDismissRequest = { showAboutDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .heightIn(max = 600.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (initialSwitchState) Color(0xFF1E1E1E) else Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
//                    Text(
//                        text = "About BLE",
//                        fontFamily = helveticaFont,
//                        style = MaterialTheme.typography.h6,
//                        textAlign = TextAlign.Center,
//                        color = if (initialSwitchState) Color.White else Color.Black,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 8.dp)
//                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "BLE Info",
                                tint = iconTint,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(top = 16.dp, bottom = 16.dp)
                            )

                            Text(
                                text = "Bluetooth Low Energy",
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.subtitle1.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                color = if (initialSwitchState) Color.White else Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )

                            Text(
                                text = "Bluetooth Low Energy (BLE) is a wireless personal area network technology " +
                                        "designed for low power consumption while maintaining a similar communication " +
                                        "range to classic Bluetooth.",
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Justify,
                                color = if (initialSwitchState) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            )

                            Text(
                                text = "Key Features:",
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.subtitle2.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (initialSwitchState) Color.White else Color.Black,
                                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 8.dp)
                            )
                        }

                        items(listOf(
                            "Low power consumption",
                            "Small data packets",
                            "Quick connection times",
                            "Secure communication",
                            "Wide device compatibility"
                        )) { feature ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Check",
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feature,
                                    fontFamily = helveticaFont,
                                    style = MaterialTheme.typography.body2,
                                    color = if (initialSwitchState) Color.White else Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { showAboutDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = iconTint
                        )
                    }
                }
            }
        }
    }
// Inside SettingsItemRow
    if (showAccountsDialog) {
        val viewModel: AuthViewModel = viewModel()
        val currentUser = viewModel.checkCurrentUser()

        AccountsDialog(
            isDarkMode = initialSwitchState,
            userName = navController?.let {
                when {
                    currentUser?.isAnonymous == true -> "Guest User"
                    currentUser != null -> currentUser.email?.substringBefore('@') ?: "User"
                    else -> "Not Signed In"
                }
            } ?: "User",
            userEmail = navController?.let {
                when {
                    currentUser?.isAnonymous == true -> "Anonymous User"
                    currentUser != null -> currentUser.email ?: ""
                    else -> ""
                }
            } ?: "",
            profilePictureUrl = currentUser?.photoUrl?.toString(), // Pass profile picture URL
            onDismiss = { showAccountsDialog = false }
        )
    }

    // New Help dialog
    if (showHelpDialog) {
        Dialog(
            onDismissRequest = { showHelpDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (initialSwitchState) Color(0xFF1E1E1E) else Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//                    Text(
//                        text = "Help",
//                        fontFamily = helveticaFont,
//                        style = MaterialTheme.typography.h6,
//                        textAlign = TextAlign.Center,
//                        color = if (initialSwitchState) Color.White else Color.Black,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 8.dp)
//                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Help,
                        contentDescription = "Help",
                        tint = iconTint,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = "For any help or to report bugs:",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        color = if (initialSwitchState) Color.White else Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:awadhropar@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Help/Support Request")
                                putExtra(Intent.EXTRA_TEXT, "Please describe your issue or question here:")
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                            showHelpDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Contact Developer",
                            color = iconTint,
                            fontFamily = helveticaFont,
                            style = MaterialTheme.typography.button
                        )
                    }

                    TextButton(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = iconTint,
                            fontFamily = helveticaFont,
                            style = MaterialTheme.typography.button
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AccountsDialog(
    isDarkMode: Boolean,
    userName: String,
    userEmail: String,
    profilePictureUrl: String? = null, // Add profile picture URL parameter
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(16.dp)
                .padding(WindowInsets.systemBars.asPaddingValues()),
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar: Use Google profile picture if available, otherwise random avatar
                if (profilePictureUrl != null) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
//                        placeholder = painterResource(R.drawable.placeholder), // Optional: Add a placeholder drawable
                        error = painterResource(R.drawable.error), // Optional: Add an error drawable
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val avatarColor = remember { generateRandomColor() }
                    val initials = userName.take(2).uppercase()

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontFamily = helveticaFont,
                            style = MaterialTheme.typography.h3.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User Name
                Text(
                    text = userName,
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // User Email
                Text(
                    text = userEmail,
                    fontFamily = helveticaFont,
                    style = MaterialTheme.typography.body2.copy(
                        color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Close",
                        color = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF),
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }
    }
}

// Helper function to generate a random color for the avatar (fallback)
fun generateRandomColor(): Color {
    val random = Random.Default
    return Color(
        red = random.nextInt(256),
        green = random.nextInt(256),
        blue = random.nextInt(256)
    ).copy(alpha = 0.8f)
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isDarkMode: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val selectedColor = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF)

    BottomNavigation(
        modifier = modifier,
        backgroundColor = backgroundColor,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.bluetooth),
                    contentDescription = "Bluetooth",
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRoute == "home_screen") selectedColor else contentColor
                )
            },
            selected = currentRoute == "home_screen",
            onClick = { navController.navigate("home_screen") }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.gamepad),
                    contentDescription = "Gameplay",
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRoute == "game_loading") selectedColor else contentColor
                )
            },
            selected = currentRoute == "game_loading",
            onClick = {
                navController.navigate("game_loading")
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    tint = if (currentRoute == "settings_screen") selectedColor else contentColor
                )
            },
            selected = currentRoute == "settings_screen",
            onClick = { navController.navigate("settings_screen") }
        )
    }
}
@Composable
fun AboutBleScreen(
    navController: NavHostController,
    isDarkMode: Boolean
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF2F2F7)
    val cardBackground = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray

    Scaffold(
        backgroundColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About BLE",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                backgroundColor = backgroundColor,
                elevation = 0.dp,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = cardBackground,
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // BLE Icon
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "BLE Info",
                        tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Bluetooth Low Energy",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = "Bluetooth Low Energy (BLE) is a wireless personal area network technology " +
                                "designed for low power consumption while maintaining a similar communication " +
                                "range to classic Bluetooth. It's ideal for applications requiring periodic " +
                                "data transfer, such as fitness trackers, smart home devices, and IoT sensors.",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.body1.copy(
                            color = secondaryTextColor
                        ),
                        textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Features
                    Text(
                        text = "Key Features:",
                        fontFamily = helveticaFont,
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "Low power consumption",
                        "Small data packets",
                        "Quick connection times",
                        "Secure communication",
                        "Wide device compatibility"
                    ).forEach { feature ->
                        Row(
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Check",
                                tint = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF007AFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                fontFamily = helveticaFont,
                                style = MaterialTheme.typography.body2.copy(
                                    color = secondaryTextColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data classes for settings remain the same
data class SettingsItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val type: SettingsItemType
)

enum class SettingsItemType {
    SWITCH,
    DETAIL
}

@Preview(showBackground = true)
@Composable
fun ModernSettingsScreenPreview() {
    // Mock NavController for preview
    val navController = rememberNavController()
    ModernSettingsScreen(onSignOut = {}, navController = navController)
}