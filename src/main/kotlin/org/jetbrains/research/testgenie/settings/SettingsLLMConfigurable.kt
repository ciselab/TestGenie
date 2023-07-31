package org.jetbrains.research.testgenie.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.research.testgenie.services.SettingsApplicationService
import javax.swing.JComponent

/**
 * This class allows to configure some LLM-related settings via the Large Language Model page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 */

class SettingsLLMConfigurable : Configurable {

    var settingsComponent: SettingsLLMComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsLLMComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        settingsComponent!!.grazieUserToken = settingsState.grazieUserToken
        settingsComponent!!.maxLLMRequest = settingsState.maxLLMRequest
        settingsComponent!!.maxPolyDepth = settingsState.maxPolyDepth
        settingsComponent!!.maxInputParamsDepth = settingsState.maxInputParamsDepth
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        var modified: Boolean = settingsComponent!!.grazieUserToken != settingsState.grazieUserToken
        modified = modified or (settingsComponent!!.maxLLMRequest != settingsState.maxLLMRequest)
        modified = modified or (settingsComponent!!.maxPolyDepth != settingsState.maxPolyDepth)
        modified = modified or (settingsComponent!!.maxInputParamsDepth != settingsState.maxInputParamsDepth)

        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        settingsState.grazieUserToken = settingsComponent!!.grazieUserToken
        settingsState.maxLLMRequest = settingsComponent!!.maxLLMRequest
        settingsState.maxPolyDepth = settingsComponent!!.maxPolyDepth
        settingsState.maxInputParamsDepth = settingsComponent!!.maxInputParamsDepth
    }

    /**
     * Returns the displayed name of the Settings tab.
     *
     * @return the name displayed in the menu (settings)
     */
    override fun getDisplayName(): String {
        return "TestGenie"
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
