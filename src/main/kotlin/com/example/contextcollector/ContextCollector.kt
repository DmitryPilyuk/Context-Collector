package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.LinkedList
import java.util.Queue

class ContextCollector(private val project: Project) {
    private val context = Context(project)
    private val members = mutableSetOf<PsiMember>()

    fun collect(testClass: PsiClass) {
        val initialMethods = testClass
            .allMethods
            .flatMap { method -> method.getCalledMethods() }

        processMethodsQueue(LinkedList(initialMethods))

        members.forEach { field -> context.add(field) }
    }

    private fun processMethodsQueue(methodsQueue: Queue<PsiMethod>) {
        val marked = HashSet<PsiMethod>()

        while (!methodsQueue.isEmpty()) {
            val method = methodsQueue.poll()
            if (method in marked) continue
            marked.add(method)
            if (method.isInProject()) {
                method
                    .getCalledMethods()
                    .filterNot { m -> m in marked }
                    .forEach { m ->
                        methodsQueue.offer(m)

                    }
                method.getAccessedFields().forEach { field ->
                    if (field.isInProject()) {
                        members.add(field)
                    } else {
                        context.addImport(field)
                    }
                }
                members.add(method)
            } else {
                context.addImport(method)
            }
        }
    }


    private fun PsiMethod.getAccessedFields() =
        PsiTreeUtil.collectElementsOfType(this, PsiReferenceExpression::class.java)
            .asSequence()
            .mapNotNull { it.resolve() as? PsiField }


    private fun PsiMethod.getCalledMethods() =
        PsiTreeUtil.collectElementsOfType(this, PsiCallExpression::class.java)
            .asSequence()
            .mapNotNull { it.resolveMethod() }

    private fun PsiMember.isInProject(): Boolean =
        this.containingFile.isInProject()

    private fun PsiClass.isInProject(): Boolean =
        this.containingFile.isInProject()

    private fun PsiFile.isInProject(): Boolean {
        this.virtualFile?.let { file ->
            val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
            return projectFileIndex.isInSourceContent(file) && !projectFileIndex.isInLibrary(file)
        }
        return false
    }

    fun printContext() {
        print(context.getText())
    }
}