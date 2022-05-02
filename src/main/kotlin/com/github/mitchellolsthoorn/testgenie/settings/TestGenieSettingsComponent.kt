package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.ui.components.JBComboBoxLabel
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class TestGenieSettingsComponent {
    var panel: JPanel? = null
    var globalTimeoutTextField = JTextField()
    var showCoverageCheckBox = JCheckBox("Do you want visualised coverage? ")
    private var sandboxCheckBox = JCheckBox("Execute tests in a sandbox environment")
    private var assertionsCheckBox = JCheckBox("Create assertions")
    private var seedTextField = JTextField()
    //DropDown menu
    var algorithmSelector = com.intellij.openapi.ui.ComboBox(arrayOf<String>("RANDOM_SEARCH","STANDARD_GA", "MONOTONIC_GA", "STEADY_STATE_GA",
            "BREEDER_GA", "CELLULAR_GA", "STANDARD_CHEMICAL_REACTION", "MAP_ELITES", "ONE_PLUS_LAMBDA_LAMBDA_GA", "ONE_PLUS_ONE_EA",
            "MU_PLUS_LAMBDA_EA", "MU_LAMBDA_EA", "MOSA", "DYNAMOSA", "LIPS", "MIO", "NSGAII", "SPEA2"))

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Global timeout "), globalTimeoutTextField, 1, false)
            .addComponent(showCoverageCheckBox, 1)
            .addComponent(sandboxCheckBox, 1)
            .addComponent(assertionsCheckBox, 1)
            .addLabeledComponent(JBLabel("Seed(random if left empty) "), seedTextField, 1, false)
            .addLabeledComponent(JBLabel("select search algorithm"), algorithmSelector, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        algorithmSelector.setMinimumAndPreferredWidth(300)
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestGenie Settings page.
     */
    fun getPreferredFocusedComponent(): JComponent {
        return globalTimeoutTextField
    }


    var globalTimeout: String?
        get() = globalTimeoutTextField.text
        set(newText) {
            globalTimeoutTextField.text = newText
        }

    var showCoverage: Boolean
        get() = showCoverageCheckBox.isSelected
        set(newStatus) {
            showCoverageCheckBox.isSelected = newStatus
        }

    var sandbox: Boolean
        get() = sandboxCheckBox.isSelected
        set(newStatus) {
            sandboxCheckBox.isSelected = newStatus
        }

    var assertions: Boolean
        get() = assertionsCheckBox.isSelected
        set(newStatus) {
            assertionsCheckBox.isSelected = newStatus
        }

    var seed: String
        get() = seedTextField.text
        set(newText) {
            seedTextField.text = newText
        }

    var algorithm: String
        get() = algorithmSelector.item
        set(newAlg) {
            algorithmSelector.item = newAlg
        }
}