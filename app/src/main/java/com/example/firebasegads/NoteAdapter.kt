package main.firebasetest

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

    override fun onBindViewHolder(h: VH, pos: Int) {
        val note = getItem(pos)
        h.b.rowTitle.text = note.title
        h.b.rowBody.text  = note.body
        h.b.rowTime.text  = note.createdAt?.let { fmt.format(it) } ?: "sending..."

        // Always handle both branches — views are recycled
        val bytes = note.image?.toBytes()
        if (bytes != null) {
            h.b.rowImage.isVisible = true
            Glide.with(h.b.rowImage).load(bytes).into(h.b.rowImage)
        } else {
            h.b.rowImage.isVisible = false
            Glide.with(h.b.rowImage).clear(h.b.rowImage)
        }

        h.b.root.setOnLongClickListener { onDelete(note); true }
    }
    companion object {
        private val fmt = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
        val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(a: Note, b: Note) = a.id == b.id
            override fun areContentsTheSame(a: Note, b: Note) = a == b
        }
    }
}