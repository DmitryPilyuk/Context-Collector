package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import java.util.LinkedList
import java.util.Queue

class ContextCollector(private val project: Project) {
    private val queue: Queue<PsiMethod> = LinkedList()
    private val context = Context(project)

    fun collect(testClass: PsiClass) {
        val marked = HashSet<PsiMethod>()
        val methods = testClass.allMethods
        methods.forEach { method -> method.allCalledMethods().forEach { m -> queue.offer(m) } }
        while (!queue.isEmpty()) {
            val method = queue.poll()
            method.allCalledMethods().forEach { m ->
                if (!marked.contains(m)) {
                    queue.offer(m)
                    marked.add(m)
                }
            }
            method.containingClass?.let { context.add(it, method) }
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
    
    fun printContext() {
        context.printContext()
    }
}