package com.example.firebasegads

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasegads.databinding.ItemNoteBinding
//import main.firebasetest.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(
    private val onDelete: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.VH>(DIFF) {
    class VH(val b: ItemNoteBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(
        ItemNoteBinding.inflate(LayoutInflater.from(p.context), p, false)
    )

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val note = getItem(pos)
        holder.b.rowTitle.text = note.title
        holder.b.rowBody.text  = note.body
        holder.b.rowTime.text  = note.createdAt?.let { fmt.format(it) } ?: "sending..."

        // Always handle both branches — views are recycled
        val bytes = note.image?.toBytes()
        holder.b.rowImage.isVisible = bytes != null
        if (bytes != null) {
            holder.b.rowImage.setImageBitmap(
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        } else {
            holder.b.rowImage.setImageDrawable(null)
        }

        holder.b.root.setOnLongClickListener { onDelete(note); true }
    }


    companion object {
        private val fmt = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
        private val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note) =
                oldItem == newItem
        }

    }
}