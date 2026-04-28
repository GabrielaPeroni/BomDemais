package com.estoque.bomdemais.notas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotasAdapter(
    private val notes: MutableList<Note>,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    class NotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNote: TextView = view.findViewById(R.id.text_nota)
        val textTimestamp: TextView = view.findViewById(R.id.text_timestamp)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_nota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val note = notes[position]
        holder.textNote.text = note.text
        holder.textTimestamp.text = dateFormat.format(Date(note.timestamp))
        holder.btnDelete.setOnClickListener { onDelete(note) }
    }

    override fun getItemCount() = notes.size

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
}
