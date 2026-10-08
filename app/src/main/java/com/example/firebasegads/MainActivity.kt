package com.example.firebasegads

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.firebasegads.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var b: ActivityMainBinding
    private var registration: ListenerRegistration? = null
    private var pendingImage: Blob? = null

    private val adapter = NoteAdapter(onDelete = ::confirmDelete)

    private val authListener = FirebaseAuth.AuthStateListener{ firebaseAuth ->
        val user = firebaseAuth.currentUser

        if (user == null) {
            registration?.remove()
            registration = null
            adapter.submitList(emptyList())
            b.emptyState.isVisible = true
        } else {
            observeNotes(user.uid)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.notesList.layoutManager = LinearLayoutManager(this)
        b.notesList.adapter = adapter

        b.saveButton.setOnClickListener { saveNote() }
        b.attachButton.setOnClickListener { pickPhoto() }

        signInAnonymouslyIfNeeded()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(authListener)
        registration?.remove()
        registration = null
        super.onStop()
    }

    private fun signInAnonymouslyIfNeeded() = lifecycleScope.launch {
        if (auth.currentUser != null) return@launch

        b.saveButton.isEnabled = false

        try {
            auth.signInAnonymously().await()
        } catch (e: Exception) {
            Snackbar.make(
                b.root,
                "Could not sign in to Firebase.",
                Snackbar.LENGTH_LONG
            ).show()
        } finally {
            b.saveButton.isEnabled = true
        }
    }

    private fun observeNotes(ownerId: String) {
        registration?.remove()

        registration = db.collection("notes")
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener(::render)
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

    private fun saveNote() = lifecycleScope.launch {
        val ownerId = auth.currentUser?.uid
        if (ownerId == null) {
            Snackbar.make(
                b.root,
                "Firebase sign-in is not ready yet.",
                Snackbar.LENGTH_LONG
            ).show()
            return@launch
        }

        val title = b.titleInput.text.toString().trim()
        if (title.isBlank()) {
            b.titleInput.error = "Add a title"
            return@launch
        }

        b.saveButton.isEnabled = false

        try {
            db.collection("notes").add(
                Note(
                    title = title,
                    body = b.bodyInput.text.toString().trim(),
                    ownerId = ownerId,
                    image = pendingImage
                )
            ).await()

            clearEditor()
        } catch (e: Exception) {
            Snackbar.make(
                b.root,
                "Note not saved: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
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
    private fun confirmDelete(note: Note) {
        if (note.id.isBlank()) return

        AlertDialog.Builder(this)
            .setTitle("Delete note?")
            .setMessage("This cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.collection("notes").document(note.id).delete().await()
                    } catch (e: Exception) {
                        Snackbar.make(
                            b.root,
                            "Note not deleted: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }


    private val picker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@registerForActivityResult

        lifecycleScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    ImageUtils.compress(applicationContext, uri)
                }

                pendingImage = Blob.fromBytes(bytes)
                b.preview.setImageBitmap(
                    BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.size)
                )
                b.preview.isVisible = true
            } catch (e: Exception) {
                Log.e("ImagePicker", "Could not prepare image", e)

                Snackbar.make(
                    b.root,
                    e.message ?: "Could not prepare that image.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun pickPhoto() = picker.launch(
        PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageOnly
        )
    )


}