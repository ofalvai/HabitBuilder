package com.ofalvai.habittracker.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray

private const val ASSET_PATH_LICENSES = "licenses.json"

data class Dependency(
    val group: String,
    val artifact: String,
    val license: License?
) {
    data class License(val name: String)
}

class LicensesViewModel(
    @SuppressLint("StaticFieldLeak") private val appContext: Context
) : ViewModel() {

    val dependencies = MutableStateFlow<List<Dependency>>(emptyList())

    init {
        dependencies.value = parseDependencies()
    }

    private fun parseDependencies(): List<Dependency> {
        val dependencies = mutableListOf<Dependency>()

        val stringContent = appContext.assets.open(ASSET_PATH_LICENSES)
            .bufferedReader()
            .use { it.readText() }

        // Ain't no need for a 3rd party JSON lib for a license screen!
        val dependencyArray = JSONArray(stringContent)
        for (i in 0 until dependencyArray.length()) {
            val dependencyObject = dependencyArray.getJSONObject(i)
            val groupId = dependencyObject.getString("groupId")
            val artifactId = dependencyObject.getString("artifactId")

            val license = if (dependencyObject.has("spdxLicenses")) {
                val licenses = dependencyObject.getJSONArray("spdxLicenses")
                if (licenses.length() > 0) {
                    val license = licenses.getJSONObject(0)
                    val licenseName = license.getString("name")
                    Dependency.License(licenseName)
                } else {
                    null
                }
            } else {
                null
            }

            dependencies.add(Dependency(groupId, artifactId, license))
        }

        return dependencies
    }

}