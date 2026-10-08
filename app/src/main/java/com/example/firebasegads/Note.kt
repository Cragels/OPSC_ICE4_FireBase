package com.example.firebasegads

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.Blob
import java.util.Date

data class Note(
    @set:DocumentId var id: String = "",

    var title: String = "",
    var body: String = "",
    var ownerId: String = "",

    // Route A — small image inline
    var image: Blob? = null,

    // Route B — a Cloud Storage download URL
    var imageUrl: String? = null,

    @ServerTimestamp
    var createdAt: Date? = null
)
