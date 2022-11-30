package com.example.presence.common

class Task(private var text: String, private var isTaskDone: Boolean) {

    fun getText() : String{
        return text
    }

    fun setText(editedText: String){
        text = editedText
    }

    fun isCheckboxChecked(): Boolean {
        return isTaskDone
    }

    fun setCheckboxChecked(isCheckboxChecked: Boolean) {
        isTaskDone = isCheckboxChecked
    }
}