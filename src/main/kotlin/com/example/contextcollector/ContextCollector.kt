package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.LinkedList
import java.util.Queue

class ContextCollector(project: Project) {
    private val context = Context(project)

    fun collect(testClass: PsiClass) {
        val initialMethods = testClass
            .allMethods
            .flatMap { method -> method.getCalledMethods() }

        processMethodsQueue(LinkedList(initialMethods))
    }

    private fun processMethodsQueue(methodsQueue: Queue<PsiMethod>) {
        val marked = HashSet<PsiMethod>()

        while (!methodsQueue.isEmpty()) {
            val method = methodsQueue.poll()

            method
                .getCalledMethods()
                .filterNot { m -> m in marked }
                .forEach { m ->
                    methodsQueue.offer(m)
                    marked.add(m)
                }
            method.getAccessedFields().forEach { field ->
                context.add(field)

            }
            context.add(method)
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