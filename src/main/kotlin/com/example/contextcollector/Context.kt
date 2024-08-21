package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager


class Context(private val project: Project) {
    private val map = HashMap<PsiClass, HashSet<PsiElement>>()

    fun add(member: PsiMember) {
        member.containingClass?.let {
            map.getOrPut(it) { HashSet() }.add(member)
        }
    }


    fun getText(): String {
        val contextClasses = buildContext()
        val strBuilder = StringBuilder()
        val codeStyleManager = CodeStyleManager.getInstance(project)
        contextClasses.forEach {
            codeStyleManager.reformat(it)
            strBuilder.append("${it.text}\n")
        }
        return strBuilder.toString()
    }


    private fun buildContext(): List<PsiClass> {
        val factory = PsiElementFactory.getInstance(project)

        val classes = map.mapNotNull { entry ->
            val analyzedClass = entry.key
            val usedElements = entry.value

            analyzedClass.name
                ?.let { factory.createClass(it) }
                ?.let { newClass ->
                    analyzedClass.annotations.forEach { annotation ->
                        newClass.modifierList?.let {
                            it.addBefore(annotation, it.firstChild)
                        }
                    }

                    usedElements.forEach { element ->
                        println("${element.text} ${analyzedClass.name}")
                        newClass.add(element)
                    }

                    newClass
                }
        }

        return classes
    }

}
