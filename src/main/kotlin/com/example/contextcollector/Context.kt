package com.example.contextcollector

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager


class Context(private val project: Project) {
    private val classes = HashMap<PsiClass, PsiClass>()
    private val imports = HashSet<PsiImportStatement>()

    fun add(member: PsiMember) {
        member.containingClass?.let { clazz ->
            classes.getOrPut(clazz) {
                val factory = PsiElementFactory.getInstance(project)
                clazz.name!!.let { name -> factory.createClass(name) }.let { newClass ->
                    clazz.annotations.forEach { annotation ->
                        newClass.modifierList?.let { it.addBefore(annotation, it.firstChild) }
                    }
                    newClass
                }
            }.add(if (member is PsiField) member.let {
                val factory = PsiElementFactory.getInstance(project)
                factory.createFieldFromText(
                    "${it.modifierList?.text} ${it.type.presentableText} ${it.name};",
                    it.context
                )
            } else member)
        }
    }

    fun addImport(member: PsiMember) {
        val factory = PsiElementFactory.getInstance(project)
        member.containingClass
            ?.let { factory.createImportStatement(it) }
            ?.let { imports.add(it) }
    }


    fun getText(): String {
        val strBuilder = StringBuilder()
        val codeStyleManager = CodeStyleManager.getInstance(project)
        imports.forEach { strBuilder.append("${it.text}\n") }
        classes.forEach { (_, clazz) ->
            codeStyleManager.reformat(clazz)
            strBuilder.append("${clazz.text}\n")
        }
        return strBuilder.toString()
    }

}
