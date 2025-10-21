package com.example.afinal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class FirestoreRepo {
    private val data=FirebaseFirestore.getInstance()

    fun favRef(string: String)=data.collection("events")
        .document(string)
        .collection("favorites")

    fun addFav(string: String, event: FavEventData)=favRef(string)
        .document(event.id)
        .set(event)

    fun unFav(string: String,eventID: String)=favRef(string)
        .document(eventID)
        .delete()

    fun loadFav(string: String, onDone:(List<FavEventData>)->Unit){favRef(string)
        .get()
        .addOnSuccessListener { snapshot ->
            val favorites = snapshot.toObjects(FavEventData::class.java)
            onDone(favorites)
        }
    }
}