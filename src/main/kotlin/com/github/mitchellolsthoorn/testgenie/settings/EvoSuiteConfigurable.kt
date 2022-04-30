package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class EvoSuiteConfigurable(private val project: Project) :
    BoundSearchableConfigurable("EvoSuite Runtime Settings", "Evo Suite") {
    private val settings
        get() = EvoSuiteRuntimeConfiguration.getInstance(project)

    private val pathToEvoSuiteJarField = pathToDirectoryTextField("Choose EvoSuite Jar")

    override fun createPanel(): DialogPanel {
        pathToEvoSuiteJarField.text = settings.evoSuiteJarPath

        return panel {
            row("EvoSuite Jar Location:") { wrapComponent(pathToEvoSuiteJarField)(CCFlags.growX, CCFlags.pushX) }

        }
    }

    override fun apply() {
        settings.evoSuiteJarPath = pathToEvoSuiteJarField.text
        super.apply()
    }

    override fun isModified(): Boolean {
        return settings.evoSuiteJarPath != pathToEvoSuiteJarField.text
    }

}

fun pathToDirectoryTextField(
    @Suppress("UnstableApiUsage") @NlsContexts.DialogTitle title: String,
): TextFieldWithBrowseButton = pathTextField(
    FileChooserDescriptorFactory.createSingleFileDescriptor(),
    title,
)


fun pathTextField(
    fileChooserDescriptor: FileChooserDescriptor,
    @NlsContexts.DialogTitle title: String,
): TextFieldWithBrowseButton {
    val component = TextFieldWithBrowseButton()
    component.addBrowseFolderListener(
        title, null, null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
    )
    return component
}


private fun wrapComponent(component: JComponent): JComponent = JPanel(BorderLayout()).apply {
    add(component, BorderLayout.NORTH)
}