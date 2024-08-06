package com.example.contextcollector

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class CollectContextAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor != null) {
            val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

            val element = findPsiElement(file, editor) ?: return
            val testMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return

            val fields = testMethod.getUsedFields()
            for (m in fields) {
                println("$m, ${m.containingClass} ")
            }
        }
    }

    private fun findPsiElement(file: PsiFile, editor: Editor): PsiElement? {
        val offset = editor.caretModel.offset
        var element = file.findElementAt(offset)
        if (element == null && offset == file.textLength) {
            element = file.findElementAt(offset - 1)
        }
        return element
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

    private fun PsiMethod.allCalledMethods(): LinkedHashSet<PsiMethod> {
        val calledMethods = LinkedHashSet<PsiMethod>()
        this.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                expression.resolveMethod()?.let { calledMethods.add(it) }
            }

        })
        return calledMethods
    }

//    private fun PsiMethod.allCalledConstructors(): LinkedHashSet<PsiMethod> {
//        val calledMethods = LinkedHashSet<PsiMethod>()
//        this.accept(object : JavaRecursiveElementVisitor() {
//            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
//                super.visitReferenceElement(reference)
//                print("$reference ")
//                reference.resolve()?.let {
//                    println(it)
//                    if (it is PsiMethod) {
//                        calledMethods.add(it)
//
//                    }
//                }
//            }
//
//        })
//        return calledMethods
//    }
}