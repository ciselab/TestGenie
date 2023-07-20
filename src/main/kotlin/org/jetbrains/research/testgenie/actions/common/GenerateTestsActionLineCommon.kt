package org.jetbrains.research.testgenie.actions.common

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.updateForLine
import org.jetbrains.research.testgenie.tools.Manager

/**
 * This class contains all the logic related to generating tests for a line by all generators in the plugin.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected line and passing it to Pipeline.
 */
class GenerateTestsActionLineCommon : AnAction() {
    /**
     * Creates and calls Pipeline to generate tests for a line when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) = Manager.generateTestsForLine(e)

    /**
     *  Makes the action visible only if a line has been selected.
     *  It also updates the action name depending on which line has been selected.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun update(e: AnActionEvent) = updateForLine(e, "all test generators")
}
