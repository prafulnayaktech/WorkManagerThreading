package com.techolution.firestore.services

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.techolution.firestore.KEY_NOTE_DATA
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.lang.Thread.sleep

class SyncCoWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    val fireStore = FirebaseFirestore.getInstance()

    @SuppressLint("LogNotTimber")
    override suspend fun doWork(): Result  = coroutineScope{
        val jobs = (0 until 1).map {
            async {
                doAsynchronousTask()
            }
        }

        // awaitAll will throw an exception if a download fails, which CoroutineWorker will treat as a failure
        jobs.awaitAll()
        Log.e("sync_","thread_ "+Thread.currentThread().id)
        Result.success()
//
    }

    private fun doAsynchronousTask() {
        Log.i("sync_","sleeping for 100 ms. thread_ "+Thread.currentThread().name)
//        sleep(5000)
        try{
            Log.i("thred_dowork"," :"+Thread.currentThread().name)
            val dataString = inputData.getString(KEY_NOTE_DATA)
            val data = Gson().fromJson(dataString, NoteData::class.java)
            makeStatusNotification("Uploading Notes", applicationContext)
            Log.i("sync_","data"+dataString)
            fireStore.collection("notes")
                .add(data)
                .addOnSuccessListener {
                    Log.i("sync_","Sync Succes")
                    saveUrl()

                }.addOnFailureListener {
                    Log.i("sync_","Sync Failed")
                    Result.failure()
                }
        }catch (e:Throwable){
            Result.failure()
        }
    }

    private fun saveUrl() {

        Log.i("sync_","sleeping for 10 sec. thread_ "+Thread.currentThread().name)
        sleep(15000)
        if(isStopped)
            Log.i("sync_","Worker Stopped")
        else
            Log.i("sync_","Worker not stopped")
        Log.i("sync_","Wake up after 10 sec")
        fireStore.collection("employee")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d("sync_employ", document.id + " => " + document.data)
                    }
                } else {
                    Log.w("sync_", "Error getting documents.", task.exception)
                }
            }
    }

}