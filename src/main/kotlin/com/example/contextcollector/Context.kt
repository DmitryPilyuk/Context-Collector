package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager

class Context(private val project: Project) {
    private val map = HashMap<PsiClass, HashSet<PsiMethod>>()

    fun add(clazz: PsiClass, method: PsiMethod) {
        map.getOrPut(clazz) { HashSet() }.add(method)
    }

    fun printContext() {
        val contextClasses = buildContext()

        val codeStyleManager = CodeStyleManager.getInstance(project)
        contextClasses.forEach {
            codeStyleManager.reformat(it)
            println(it.text)
        }
    }

    private fun buildContext(): List<PsiClass> {
        val factory = PsiElementFactory.getInstance(project)

        val classes = map.mapNotNull { entry ->
            val analyzedClass = entry.key
            val usedMethods = entry.value

            entry.key.name
                ?.let { factory.createClass(it) }
                ?.let { newClass ->
                    analyzedClass.annotations.forEach { annotation ->
                        newClass.modifierList?.let {
                            it.addBefore(annotation, it.firstChild)
                        }
                    }

                    usedMethods.forEach { method ->
                        newClass.add(method)
                    }

                    usedMethods
                        .flatMap { it.getUsedFields() }.toHashSet()
                        .forEach { field -> newClass.add(field) }

                    newClass
                }
        }

        return classes
    }

    private fun PsiMethod.getUsedFields(): HashSet<PsiField> {
        val fields = HashSet<PsiField>()
        this.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                super.visitReferenceExpression(expression)
                expression.resolve()?.let {
                    if (it is PsiField && it.containingClass == this@getUsedFields.containingClass) {
                        fields.add(it)
                    }
                }
            }
        })
        return fields
    }

    /**
     * Try
     * override fun getAccessedFields() =
     *         PsiTreeUtil.collectElementsOfType(psiMethod, PsiReferenceExpression::class.java)
     *             .asSequence()
     *             .mapNotNull { it.resolve() as? PsiField }
     */
}
