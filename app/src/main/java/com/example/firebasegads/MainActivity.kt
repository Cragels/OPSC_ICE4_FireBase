package com.example.firebasegads

import android.os.Bundle
import android.util.Log
//import androidx.activity.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasegads.databinding.ActivityMainBinding

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.auth
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


}