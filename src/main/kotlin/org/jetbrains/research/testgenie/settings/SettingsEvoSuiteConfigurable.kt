package org.jetbrains.research.testgenie.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import org.jetbrains.research.testgenie.services.SettingsApplicationService
import javax.swing.JComponent

/**
 * This class allows to configure some EvoSuite settings via the EvoSuite page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 * It interacts with the SettingsEvoSuiteComponent, TestGenieSettingsService and TestGenieSettingsState.
 * It provides controller functionality for the TestGenieSettingsState.
 */
class SettingsEvoSuiteConfigurable : Configurable {

    var settingsComponent: SettingsEvoSuiteComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsEvoSuiteComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        settingsComponent!!.javaPath = settingsState.javaPath
        settingsComponent!!.sandbox = settingsState.sandbox
        settingsComponent!!.assertions = settingsState.assertions
        settingsComponent!!.seed = settingsState.seed
        settingsComponent!!.algorithm = settingsState.algorithm
        settingsComponent!!.configurationId = settingsState.configurationId
        settingsComponent!!.clientOnThread = settingsState.clientOnThread
        settingsComponent!!.junitCheck = settingsState.junitCheck
        settingsComponent!!.criterionLine = settingsState.criterionLine
        settingsComponent!!.criterionBranch = settingsState.criterionBranch
        settingsComponent!!.criterionException = settingsState.criterionException
        settingsComponent!!.criterionWeakMutation = settingsState.criterionWeakMutation
        settingsComponent!!.criterionOutput = settingsState.criterionOutput
        settingsComponent!!.criterionMethod = settingsState.criterionMethod
        settingsComponent!!.criterionMethodNoException = settingsState.criterionMethodNoException
        settingsComponent!!.criterionCBranch = settingsState.criterionCBranch
        settingsComponent!!.minimize = settingsState.minimize
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        var modified: Boolean = settingsComponent!!.sandbox != settingsState.sandbox
        modified = modified or (settingsComponent!!.javaPath != settingsState.javaPath)
        modified = modified or (settingsComponent!!.assertions != settingsState.assertions)
        modified = modified or (settingsComponent!!.seed != settingsState.seed)
        modified = modified or (settingsComponent!!.algorithm != settingsState.algorithm)
        modified = modified or (settingsComponent!!.configurationId != settingsState.configurationId)
        modified = modified or (settingsComponent!!.clientOnThread != settingsState.clientOnThread)
        modified = modified or (settingsComponent!!.junitCheck != settingsState.junitCheck)
        modified = modified or (settingsComponent!!.criterionLine != settingsState.criterionLine)
        modified = modified or (settingsComponent!!.criterionBranch != settingsState.criterionBranch)
        modified = modified or (settingsComponent!!.criterionException != settingsState.criterionException)
        modified = modified or (settingsComponent!!.criterionWeakMutation != settingsState.criterionWeakMutation)
        modified = modified or (settingsComponent!!.criterionOutput != settingsState.criterionOutput)
        modified = modified or (settingsComponent!!.criterionMethod != settingsState.criterionMethod)
        modified = modified or (settingsComponent!!.criterionMethodNoException != settingsState.criterionMethodNoException)
        modified = modified or (settingsComponent!!.criterionCBranch != settingsState.criterionCBranch)
        modified = modified or (settingsComponent!!.minimize != settingsState.minimize)
        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        settingsState.javaPath = settingsComponent!!.javaPath
        settingsState.sandbox = settingsComponent!!.sandbox
        settingsState.assertions = settingsComponent!!.assertions
        settingsState.algorithm = settingsComponent!!.algorithm
        settingsState.configurationId = settingsComponent!!.configurationId
        settingsState.clientOnThread = settingsComponent!!.clientOnThread
        settingsState.junitCheck = settingsComponent!!.junitCheck
        settingsState.criterionLine = settingsComponent!!.criterionLine
        settingsState.criterionBranch = settingsComponent!!.criterionBranch
        settingsState.criterionException = settingsComponent!!.criterionException
        settingsState.criterionWeakMutation = settingsComponent!!.criterionWeakMutation
        settingsState.criterionOutput = settingsComponent!!.criterionOutput
        settingsState.criterionMethod = settingsComponent!!.criterionMethod
        settingsState.criterionMethodNoException = settingsComponent!!.criterionMethodNoException
        settingsState.criterionCBranch = settingsComponent!!.criterionCBranch
        settingsState.minimize = settingsComponent!!.minimize

        val seed = settingsComponent!!.seed.toLongOrNull()
        if (settingsComponent!!.seed != "" && seed == null) {
            Messages.showErrorDialog(
                "Seed parameter is not of numeric type. Therefore, it will not be saved. However, the rest of the parameters have been successfully saved.",
                "Incorrect Numeric Type For Seed"
            )
            return
        }
        settingsState.seed = settingsComponent!!.seed
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
     * Returns the UI component that should be focused when the TestGenie Settings page is opened.
     *
     *  @return preferred UI component
     */
    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.getPreferredFocusedComponent()
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
