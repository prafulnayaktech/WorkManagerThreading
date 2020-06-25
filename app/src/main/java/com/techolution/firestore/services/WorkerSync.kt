package com.techolution.firestore.services

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings




class WorkerSync(ctx:Context, param:WorkerParameters): Worker(ctx,param) {

    override fun doWork(): Result {
        val appContext = applicationContext
        makeStatusNotification("Uploading Notes", appContext)

        val user: MutableMap<String, Any> = HashMap()
        user["first"] = "Somya"
        user["middle"] = "Charan"
        user["last"] = "Pille"
        user["born"] = 1982

        Log.d("firestore_", "firestore_ ")

        val firestore = FirebaseFirestore.getInstance()

//         Add a new document with a generated ID
        firestore.collection("employee")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("firestore_", "DocumentSnapshot added with ID: " + documentReference.id)
                }.addOnFailureListener { e ->
                    Log.w("firestore_", "Error adding document", e)
                }


        return Result.success()
    }
}