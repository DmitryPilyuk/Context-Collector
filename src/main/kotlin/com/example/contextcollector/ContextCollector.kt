package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.LinkedList
import java.util.Queue

class ContextCollector(project: Project) {
    private val context = Context(project)
    private val members = mutableSetOf<PsiMember>()

    fun collect(testClass: PsiClass) {
        val initialMethods = testClass
            .allMethods
            .flatMap { method -> method.getCalledMethods() }

        processMethodsQueue(LinkedList(initialMethods), (testClass.containingFile as PsiJavaFile).packageName)

        members.forEach { field -> context.add(field) }
    }

    private fun processMethodsQueue(methodsQueue: Queue<PsiMethod>, packageName: String) {
        val marked = HashSet<PsiMethod>()

        while (!methodsQueue.isEmpty()) {
            val method = methodsQueue.poll()
            if (method in marked) continue
            marked.add(method)
            if ((method.containingFile as PsiJavaFile).packageName.startsWith(packageName)) {
                method
                    .getCalledMethods()
                    .filterNot { m -> m in marked }
                    .forEach { m ->
                        methodsQueue.offer(m)

                    }
                method.getAccessedFields().forEach { field ->
                    if ((field.containingFile as PsiJavaFile).packageName.startsWith(packageName)) {
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


    fun printContext() {
        print(context.getText())
    }
}