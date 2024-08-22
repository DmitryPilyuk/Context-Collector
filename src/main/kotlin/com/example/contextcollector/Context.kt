package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager


class Context(private val project: Project) {
    private val map = HashMap<PsiClass, PsiClass>()

    fun add(member: PsiMember) {
        member.containingClass?.let { clazz ->
            map.getOrPut(clazz) {
                val factory = PsiElementFactory.getInstance(project)
                clazz.name!!.let { name -> factory.createClass(name) }.let { newClass ->
                    clazz.annotations.forEach { annotation ->
                        newClass.modifierList?.let { it.addBefore(annotation, it.firstChild) }
                    }
                    newClass
                }
            }.add(member)
        }
    }


    fun getText(): String {
        val strBuilder = StringBuilder()
        val codeStyleManager = CodeStyleManager.getInstance(project)
        map.forEach { (_, clazz) ->
            codeStyleManager.reformat(clazz)
            strBuilder.append("${clazz.text}\n")
        }
        return strBuilder.toString()
    }

}
