package nl.tudelft.ewi.se.ciselab.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner

/**
 * This class generates tests for a class.
 */
class GenerateTestsActionClass : AnAction() {
    private val log = Logger.getInstance(this.javaClass)

    /**
     * Performs test generation for a class when the action is invoked.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun actionPerformed(e: AnActionEvent) {
        // determine class path
        val project: Project = e.project ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        val psiClass = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT) as PsiClass // Checked in update method
        val classFQN = psiClass.qualifiedName ?: return

        log.info("Selected class is $classFQN")

        Runner(project, projectPath, projectClassPath, classFQN).forClass().runEvoSuite()
    }

    /**
     * Makes the action visible only if a class has been selected.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun update(e: AnActionEvent) {
        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isEnabledAndVisible = psiElement is PsiClass // TODO: check for the current project
    }
}
