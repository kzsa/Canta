package org.samo_lego.canta.util

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.io.File
import java.net.URL

private const val BLOAT_URL =
    "https://raw.githubusercontent.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation/main/resources/assets/uad_lists.json"
private const val BLOAT_COMMITS =
    "https://api.github.com/repos/Universal-Debloater-Alliance/universal-android-debloater-next-generation/commits?path=resources%2Fassets%2Fuad_lists.json"


class BloatUtils {
    fun fetchBloatList(uadList: File, config: File): JSONObject {
        try {
            // Fetch json from BLOAT_URL and parse it
            val response = URL(BLOAT_URL).readText()
            // Parse response to json
            val json = JSONObject(response)

            val commits = URL(BLOAT_COMMITS).readText()
            // Parse commits to get latest commit hash
            val hash = commits.split("\"sha\":\"")[1].split("\"")[0]

            // Write json to file
            uadList.writeText(json.toString())
            // Write latest commit hash to file
            config.writeText(hash)

            return json
        } catch (e: Exception) {
            return JSONObject()
        }
    }

    fun checkForUpdates(config: File): Boolean {
        return try {
            val commits = URL(BLOAT_COMMITS).readText()
            // Parse commits to get latest commit hash
            val hash = commits.split("\"sha\":\"")[1].split("\"")[0]

            // Read config file
            val configHash = config.readText()

            hash != configHash
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * App bloat information, parsed from the UAD json.
 */
@Parcelize
data class BloatData(
    internal val installData: InstallData?,
    internal val description: String?,
    internal val removal: RemovalRecommendation?,
) : Parcelable {
    companion object {
        fun fromJson(json: JSONObject): BloatData {
            val installData = InstallData.byNameIgnoreCaseOrNull(json.getString("list"))
            val description = json.getString("description")
            val removal = RemovalRecommendation.byNameIgnoreCaseOrNull(json.getString("removal"))

            return BloatData(installData, description, removal)
        }
    }
}

/**
 * Enum class to represent the removal recommendation, from the UAD list.
 */
enum class RemovalRecommendation(
    val icon: ImageVector,
    val badgeColor: Color,
    val description: String
) {
    RECOMMENDED(
        Icons.Default.Check,
        Color.Green,
        "Pointless or outright negative packages, and/or apps available through Google Play."
    ),
    ADVANCED(
        Icons.Default.Settings,
        Color.Yellow,
        "Breaks obscure or minor parts of functionality, or apps that aren't easily enabled/installed through Settings/Google Play. This category is also used for apps that are useful (default keyboard/gallery/launcher/music app.) but that can easily be replaced by a better alternative."
    ),
    EXPERT(
        Icons.Default.Warning,
        Color.Red,
        "Breaks widespread and/or important functionality, but nothing important to the basic operation of the operating system. Removing an 'Expert' package should not bootloop the device (unless mentioned in the description) but we can't guarantee it 100%."
    ),
    UNSAFE(
        Icons.Default.Close,
        Color.Magenta,
        "Can break vital parts of the operating system. Removing an 'Unsafe' package have an extremely high risk of bootlooping your device."
    ),
    SYSTEM(
        Icons.Default.Android,
        Color.DarkGray,
        "System apps are apps that come pre-installed with your device."
    );

    companion object {
        fun byNameIgnoreCaseOrNull(input: String): RemovalRecommendation? {
            return entries.firstOrNull { it.name.equals(input, true) }
        }
    }
}

/**
 * Represents the install data from the UAD list.
 */
enum class InstallData {
    OEM,
    CARRIER;

    companion object {
        fun byNameIgnoreCaseOrNull(input: String): InstallData? {
            return entries.firstOrNull { it.name.equals(input, true) }
        }
    }
}
