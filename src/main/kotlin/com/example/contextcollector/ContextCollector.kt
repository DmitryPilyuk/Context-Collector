package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import java.util.LinkedList
import java.util.Queue

class ContextCollector(project: Project) {
    private val context = Context(project)

    fun collect(testClass: PsiClass) {
        val initialMethods = testClass
            .allMethods
            .flatMap { method -> method.allCalledMethods() }

        processMethodsQueue(LinkedList(initialMethods))
    }

    private fun processMethodsQueue(methodsQueue: Queue<PsiMethod>) {
        val marked = HashSet<PsiMethod>()

        while (!methodsQueue.isEmpty()) {
            val method = methodsQueue.poll()

            method
                .allCalledMethods()
                .filterNot { m -> m in marked }
                .forEach { m ->
                    methodsQueue.offer(m)
                    marked.add(m)
                }

            method.containingClass
                ?.let { context.add(it, method) }
        }
    }

    private fun PsiMethod.allCalledMethods(): HashSet<PsiMethod> {
        val calledMethods = HashSet<PsiMethod>()
        this.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                expression.resolveMethod()?.let { calledMethods.add(it) }
            }

            override fun visitNewExpression(expression: PsiNewExpression) {
                super.visitNewExpression(expression)
                expression.resolveMethod()?.let { calledMethods.add(it) }
            }
        })
        return calledMethods
    }

    /**
     * fun getCalledMethods() =
     *         PsiTreeUtil.collectElementsOfType(psiMethod, PsiCallExpression::class.java)
     *             .asSequence()
     *             .mapNotNull { it.resolveMethod() }
     */

    fun printContext() {
        context.printContext()
    }
}