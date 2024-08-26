package com.example.contextcollector

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil


class CollectContextAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor != null) {
            val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

            val element = findPsiElement(file, editor) ?: return
            val testClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return
            val contextCollector = ContextCollector(project)
            contextCollector.collect(testClass)
            contextCollector.printContext()

        }
    }

    private fun findPsiElement(file: PsiFile, editor: Editor): PsiElement? {
        val offset = editor.caretModel.offset
        var element = file.findElementAt(offset)
        if (element == null && offset == file.textLength) {
            element = file.findElementAt(offset - 1)
        }
        return element
    }
}