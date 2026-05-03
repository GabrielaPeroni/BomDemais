package com.estoque.bomdemais.notas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Note
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotasAdapter(
    private val notes: MutableList<Note>,
    private val onTap: (Note) -> Unit,
    private val onLongPress: (Note) -> Unit,
    private val onSelectionChanged: (count: Int) -> Unit
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    var isSelectionMode = false
        private set
    private val selectedIds = mutableSetOf<String>()

    class NotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNote: TextView = view.findViewById(R.id.text_nota)
        val textTimestamp: TextView = view.findViewById(R.id.text_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val note = notes[position]
        val isSelected = selectedIds.contains(note.id)

        holder.textNote.text = note.title.ifEmpty { note.body }
        holder.textTimestamp.text = dateFormat.format(Date(note.timestamp))

        (holder.itemView as MaterialCardView).setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(holder.itemView.context, R.color.card_selected_bg)
            else MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSurface)
        )

        holder.itemView.setOnClickListener {
            if (isSelectionMode) toggleSelection(note.id)
            else onTap(note)
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) onLongPress(note)
            true
        }
    }

    override fun getItemCount() = notes.size

    fun getItemAt(position: Int): Note = notes[position]

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    fun addNote(note: Note) {
        notes.add(0, note)
        notifyItemInserted(0)
    }

    fun removeNote(note: Note) {
        val position = notes.indexOf(note)
        if (position >= 0) {
            notes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun enterSelectionMode(id: String) {
        isSelectionMode = true
        selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Note> = notes.filter { selectedIds.contains(it.id) }

    private fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }
}
