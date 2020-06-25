package com.techolution.firestore.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.techolution.firestore.KEY_NOTE_DATA

class SyncCoWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    val fireStore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        return try{
            val dataString = inputData.getString(KEY_NOTE_DATA)
            val data = Gson().fromJson(dataString, NoteData::class.java)
            makeStatusNotification("Uploading Notes", applicationContext)
            Log.i("sync_","data"+dataString)
            fireStore.collection("notes")
                .add(data)
                .addOnSuccessListener {
                    Log.i("sync_","Sync Succes")
//                    saveUrl()
                    Result.success()

                }.addOnFailureListener {
                    Log.i("sync_","Sync Succes")
                    Result.failure()
                }
            Result.success()
        }catch (e:Throwable){
            Result.failure()
        }
    }
}