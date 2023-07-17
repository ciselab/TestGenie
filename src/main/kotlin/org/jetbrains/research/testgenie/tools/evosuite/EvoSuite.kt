package org.jetbrains.research.testgenie.tools.evosuite

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testgenie.actions.createPipeline
import org.jetbrains.research.testgenie.actions.getSurroundingLine
import org.jetbrains.research.testgenie.actions.getSurroundingMethod
import org.jetbrains.research.testgenie.data.CodeType
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.helpers.generateMethodDescriptor
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.tools.template.Tool
import org.jetbrains.research.testgenie.tools.evosuite.generation.EvoSuiteProcessManager

class EvoSuite(override val name: String = "EvoSuite") : Tool {
    private fun getEvoSuiteProcessManager(e: AnActionEvent): EvoSuiteProcessManager {
        val project: Project = e.project!!
        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val settingsProjectState = project.service<SettingsProjectService>().state
        val buildPath = "$projectPath/${settingsProjectState.buildPath}"
        val vFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)!!
        val fileUrl = vFile.presentableUrl
        val modificationStamp = vFile.modificationStamp
        return EvoSuiteProcessManager(project, projectPath, buildPath, fileUrl, modificationStamp)
    }

    override fun generateTestsForClass(e: AnActionEvent) {
        createPipeline(e).runTestGeneration(getEvoSuiteProcessManager(e), CodeTypeAndAdditionData(CodeType.CLASS))
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        val methodDescriptor = generateMethodDescriptor(psiMethod)
        createPipeline(e).runTestGeneration(getEvoSuiteProcessManager(e), CodeTypeAndAdditionData(CodeType.METHOD, methodDescriptor))
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        createPipeline(e).runTestGeneration(getEvoSuiteProcessManager(e), CodeTypeAndAdditionData(CodeType.LINE, selectedLine))
    }
}
