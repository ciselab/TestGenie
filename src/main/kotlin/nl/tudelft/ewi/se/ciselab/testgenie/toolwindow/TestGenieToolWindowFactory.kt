package nl.tudelft.ewi.se.ciselab.testgenie.toolwindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseDisplayService

/**
 * This class is responsible for creating the UI of the TestGenie tool window.
 */
class TestGenieToolWindowFactory : ToolWindowFactory {
    /**
     * Initialises the UI of the tool window.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val quickAccessParameters = QuickAccessParameters(project)
        val contentFactory: ContentFactory = ContentFactory.SERVICE.getInstance()

        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        toolWindow.contentManager.addContent(
            contentFactory.createContent(
                testCaseDisplayService.mainPanel, "Generated Tests", true
            )
        )

        val content: Content = contentFactory.createContent(quickAccessParameters.getContent(), "Parameters", false)
        toolWindow.contentManager.addContent(content)
    }
}
