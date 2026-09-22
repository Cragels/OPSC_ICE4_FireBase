package main.firebasetest

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.Blob
import java.util.Date

data class Note(
    @DocumentId
    val id: String = "",

    val title: String = "",
    val body: String = "",
    val ownerId: String = "",

    // Route A — small image inline
    val image: Blob? = null,

    // Route B — a Cloud Storage download URL
    val imageUrl: String? = null,

    @ServerTimestamp
    val createdAt: Date? = null
)
