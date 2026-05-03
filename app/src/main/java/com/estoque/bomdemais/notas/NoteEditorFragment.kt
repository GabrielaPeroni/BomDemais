package com.estoque.bomdemais.notas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Note
import com.google.android.material.appbar.MaterialToolbar

class NoteEditorFragment : Fragment() {

    private lateinit var editTitle: EditText
    private lateinit var editBody: EditText
    private val viewModel: NotasViewModel by viewModels { NotasViewModel.Factory }
    private var existingNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            val noteId = args.getString(ARG_NOTE_ID)
            if (noteId != null) {
                existingNote = Note(
                    id = noteId,
                    title = args.getString(ARG_NOTE_TITLE, ""),
                    body = args.getString(ARG_NOTE_BODY, ""),
                    timestamp = args.getLong(ARG_NOTE_TIMESTAMP, 0)
                )
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_note_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.left_return_arrow)
        toolbar.setNavigationOnClickListener { saveAndPop() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { saveAndPop() }
        })

        editTitle = view.findViewById(R.id.edit_note_title)
        editBody = view.findViewById(R.id.edit_note_text)

        existingNote?.let {
            editTitle.setText(it.title)
            editBody.setText(it.body)
        }
        editBody.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.navigationIcon = null
    }

    private fun saveAndPop() {
        val title = editTitle.text.toString().trim()
        val body = editBody.text.toString().trim()
        if (title.isNotEmpty() || body.isNotEmpty()) {
            val note = existingNote
            if (note != null) viewModel.editNote(note, title, body)
            else viewModel.addNote(title, body)
        }
        parentFragmentManager.popBackStack()
    }

    companion object {
        private const val ARG_NOTE_ID = "note_id"
        private const val ARG_NOTE_TITLE = "note_title"
        private const val ARG_NOTE_BODY = "note_body"
        private const val ARG_NOTE_TIMESTAMP = "note_timestamp"

        fun newInstance(note: Note? = null) = NoteEditorFragment().apply {
            arguments = Bundle().apply {
                note?.let {
                    putString(ARG_NOTE_ID, it.id)
                    putString(ARG_NOTE_TITLE, it.title)
                    putString(ARG_NOTE_BODY, it.body)
                    putLong(ARG_NOTE_TIMESTAMP, it.timestamp)
                }
            }
        }
    }
}
