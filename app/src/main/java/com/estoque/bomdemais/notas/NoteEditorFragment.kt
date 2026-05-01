package com.estoque.bomdemais.notas

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Note

class NoteEditorFragment : DialogFragment() {

    private lateinit var editText: EditText
    private var existingNote: Note? = null
    private var onSave: ((String) -> Unit)? = null
    private var saved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_note_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_note_editor)
        editText = view.findViewById(R.id.edit_note_text)

        existingNote?.let { editText.setText(it.text) }
        editText.requestFocus()

        toolbar.setNavigationOnClickListener { saveAndDismiss() }

        toolbar.inflateMenu(R.menu.note_editor_menu)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_save) {
                saveAndDismiss()
                true
            } else false
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCancel(dialog: android.content.DialogInterface) {
        saveAndDismiss()
        super.onCancel(dialog)
    }

    private fun saveAndDismiss() {
        if (saved) return
        saved = true
        val text = editText.text.toString().trim()
        if (text.isNotEmpty()) onSave?.invoke(text)
        dismiss()
    }

    companion object {
        fun forNew(onSave: (String) -> Unit): NoteEditorFragment {
            return NoteEditorFragment().also { it.onSave = onSave }
        }

        fun forEdit(note: Note, onSave: (String) -> Unit): NoteEditorFragment {
            return NoteEditorFragment().also {
                it.existingNote = note
                it.onSave = onSave
            }
        }
    }
}
