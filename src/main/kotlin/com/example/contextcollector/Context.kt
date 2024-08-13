package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager

class Context(private val project: Project) {
    private val map = HashMap<PsiClass, HashSet<PsiMethod>>()


    fun add(clazz: PsiClass, method: PsiMethod) {
        if (!map.containsKey(clazz)) {
            map[clazz] = HashSet()
            map[clazz]?.add(method)
        } else {
            map[clazz]?.add(method)
        }
    }

    private fun buildContext(): HashSet<PsiClass> {
        val factory = PsiElementFactory.getInstance(project)
        val codeStyleManager = CodeStyleManager.getInstance(project)
        val classes = HashSet<PsiClass>()
        map.forEach { entry ->
            val fields = HashSet<PsiField>()
            val clazz = entry.key
            val newClass = entry.key.name?.let { factory.createClass(it) }
            if (newClass != null) {
                clazz.annotations.forEach { annotation ->
                    newClass.modifierList?.let { it.addBefore(annotation, it.firstChild) }
                }
                entry.value.forEach { method ->
                    fields.addAll(method.getUsedFields())
                    newClass.add(method)

                }
                fields.forEach { field ->
                    newClass.add(field)
                }
                codeStyleManager.reformat(newClass)
                classes.add(newClass)
            }
        }
        return classes
    }

    fun printContext() {
        val set = buildContext()
        for (clazz in set) {
            println(clazz.text)
        }


    }

    private fun PsiMethod.getUsedFields(): HashSet<PsiField> {
        val fields = HashSet<PsiField>()
        this.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                super.visitReferenceExpression(expression)
                expression.resolve()?.let {
                    if (it is PsiField) {
                        fields.add(it)
                    }
                }
            }
        })
        return fields
    }
}