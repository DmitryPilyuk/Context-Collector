package com.example.contextcollector

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager


class Context(project: Project) {
    private val classes = HashMap<PsiClass, PsiClass>()
    private val imports = HashSet<String>()
    private val factory = PsiElementFactory.getInstance(project)
    private val psiFileFactory = PsiFileFactory.getInstance(project)
    private val codeStyleManager = CodeStyleManager.getInstance(project)

    fun add(member: PsiMember) {
        member.containingClass?.let { clazz ->
            classes.getOrPut(clazz) {
                clazz.name!!.let { name -> factory.createClass(name) }.let { newClass ->
                    clazz.annotations.forEach { annotation ->
                        newClass.modifierList?.let { it.addBefore(annotation, it.firstChild) }
                    }
                    newClass
                }
            }.add(member)

//            add(if (member is PsiField) member.let {
//                factory.createFieldFromText(
//                    "${it.modifierList?.text} ${it.type.presentableText} ${it.name};",
//                    it.context
//                )
//            } else member)
        }
    }

    fun addImport(member: PsiMember) {
        member.containingClass
            ?.let { factory.createImportStatement(it) }
            ?.let { imports.add(it.text) }
    }


    fun getText(): String {
        val strBuilder = StringBuilder()
        imports.forEach { strBuilder.append("${it}\n") }
        strBuilder.append(("\n"))
        classes.forEach { (_, clazz) ->
            codeStyleManager.reformat(clazz)
            strBuilder.append("${clazz.text}\n")
        }
        return strBuilder.toString()
    }

    fun getPsiFile(): PsiFile =
        psiFileFactory.createFileFromText("Context.java", JavaLanguage.INSTANCE, getText())
}
