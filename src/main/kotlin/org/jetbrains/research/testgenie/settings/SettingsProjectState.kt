package org.jetbrains.research.testgenie.settings

import org.jetbrains.research.testgenie.TestGenieDefaultsBundle

/**
 * This class is the actual data class that stores the values of the Plugin Settings entries.
 */
data class SettingsProjectState(
    var colorRed: Int = DefaultSettingsPluginState.colorRed,
    var colorGreen: Int = DefaultSettingsPluginState.colorGreen,
    var colorBlue: Int = DefaultSettingsPluginState.colorBlue,
    var buildPath: String = DefaultSettingsPluginState.buildPath,
    var buildCommand: String = DefaultSettingsPluginState.buildCommand,
    var telemetryEnabled: Boolean = DefaultSettingsPluginState.telemetryEnabled,
    var telemetryPath: String = DefaultSettingsPluginState.telemetryPath
) {
    /**
     * Default values of SettingsProjectState.
     */
    object DefaultSettingsPluginState {
        val javaPath: String = TestGenieDefaultsBundle.defaultValue("javaPath")
        val colorRed: Int = TestGenieDefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = TestGenieDefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = TestGenieDefaultsBundle.defaultValue("colorBlue").toInt()
        const val buildPath: String = ""
        const val buildCommand: String = ""
        val telemetryEnabled: Boolean = TestGenieDefaultsBundle.defaultValue("telemetryEnabled").toBoolean()
        val telemetryPath: String = System.getProperty("user.home")
    }
}
