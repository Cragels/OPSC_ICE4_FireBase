package com.example.firebasegads

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.R
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasegads.ImageUtils.compress
import com.example.firebasegads.databinding.ActivityMainBinding
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


//import main.firebasetest.NoteAdapter

//import main.firebasetest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val adapter = NoteAdapter(onDelete = ::confirmDelete)

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.notesList.layoutManager = LinearLayoutManager(this)
        b.notesList.adapter = adapter

        b.saveButton.setOnClickListener { saveNote() }
        b.attachButton.setOnClickListener { pickPhoto() }
    }

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val uid get() = auth.currentUser?.uid



    private var registration: ListenerRegistration? = null

    override fun onStart() {
        super.onStart()
        registration = db.collection("notes")
            .whereEqualTo("ownerId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err -> render(snap, err) }
    }

    private fun render(snapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
    {
        if (error != null) {
            Log.e("Firestore", "Could not load notes", error)
            return
        }

        val notes = snapshot?.toObjects(Note::class.java).orEmpty()
        adapter.submitList(notes)
        b.emptyState.isVisible = notes.isEmpty()
    }

    override fun onStop() {
        super.onStop()
        registration?.remove()
        registration = null
    }

    private fun confirmDelete(note: Note) {
        val snapshot = note          // keep a copy in memory
        db.collection("notes").document(note.id).delete()

        Snackbar.make(b.root, "Note deleted", LENGTH_LONG)
            .setAction("Undo") {
                db.collection("notes")
                    .document(snapshot.id)
                    .set(snapshot)
            }
            .show()
    }

    private fun saveNote() = lifecycleScope.launch {
        b.saveButton.isEnabled = false
        try {
            val owner = auth.currentUser?.uid
                ?: auth.signInAnonymously().await().user!!.uid

            db.collection("notes").add(
                Note(
                    title   = b.titleInput.text.toString().trim(),
                    ownerId = owner,
                    image   = pendingImage
                )
            ).await()

            clearEditor()
        } catch (e: Exception) {
            Snackbar.make(b.root, "Note not saved", LENGTH_LONG).show()
        } finally {
            b.saveButton.isEnabled = true
        }
    }

    private fun clearEditor() {
        b.titleInput.text?.clear()
        b.bodyInput.text?.clear()
        pendingImage = null
        b.preview.setImageDrawable(null)
        b.preview.isVisible = false
    }
    private var pendingImage: Blob? = null

    private val picker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val bytes = withContext(Dispatchers.IO) { compress(applicationContext,uri) }
            pendingImage = Blob.fromBytes(bytes)
            b.preview.setImageBitmap(
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            )
            b.preview.isVisible = true
        }
    }

    private fun pickPhoto() = picker.launch(
        PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageOnly
        )
    )

}