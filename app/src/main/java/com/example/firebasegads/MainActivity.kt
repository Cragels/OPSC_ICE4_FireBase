package main.firebasetest

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
import com.example.firebasegads.databinding.ActivityMainBinding
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
//import main.firebasetest.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream

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

    private fun saveNote() {
        val owner = uid ?: return
        val title = b.titleInput.text.toString().trim()
        if (title.isEmpty()) {
            b.titleInput.error = "Add a title"
            return
        }

        val note = Note(
            title   = title,
            body    = b.bodyInput.text.toString().trim(),
            ownerId = owner,
            image   = pendingImage        // Blob? from the picker
        )

        db.collection("notes")
            .add(note)
            .addOnSuccessListener { clearEditor() }
            .addOnFailureListener {
                Snackbar.make(b.root, "Note not saved. Try again.",
                    Snackbar.LENGTH_LONG).show()
            }


    }



}