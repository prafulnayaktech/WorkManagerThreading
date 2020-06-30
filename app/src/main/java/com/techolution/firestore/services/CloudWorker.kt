package com.techolution.firestore.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.techolution.firestore.KEY_NOTE_DATA
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Thread.sleep


class CloudWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    val fireStore = FirebaseFirestore.getInstance()

    @SuppressLint("LogNotTimber")
    override fun doWork(): Result {
        Log.i("thred_dowork"," :"+Thread.currentThread().name)
        val appContext = applicationContext
        val dataString = inputData.getString(KEY_NOTE_DATA)
        val data = Gson().fromJson(dataString, NoteData::class.java)
        makeStatusNotification("Uploading Notes", appContext)
        Log.i("uri:"," ${data.uri}")

        val resolver = appContext.contentResolver

        appContext.grantUriPermission(appContext.packageName,
            Uri.parse(data.uri),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )


        val f = File(data.uri, "profile.jpg")
        val picture = BitmapFactory.decodeStream(FileInputStream(f))
        // Create a storage reference from our app
        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference

        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageDataX: ByteArray = baos.toByteArray()

        var pathX = "images/"+System.currentTimeMillis().toString()+".jpg"
        val mountainImagesRef = storageRef.child(pathX)
        val uploadTask = mountainImagesRef.putBytes(imageDataX)

        uploadTask.addOnSuccessListener {
                Log.i("sync_","Image uploaded"+ it.metadata!!.path+"   :  "+it.metadata!!.name)
            data.uri= it.metadata!!.path
            fireStore.collection("notes")
                .add(data)
                .addOnSuccessListener {
                    Log.i("sync_","Sync Succes")
                    if(isStopped)
                        Log.i("sync_","Worker Stopped")
                    else
                        saveUrl()

                }.addOnFailureListener {
                    Log.i("sync_","Sync Succes")
                }
        }.addOnFailureListener{
            Log.e("sync_","Image not uploaded"+it.toString())
        }
        Timber.i("sync_data: $dataString")

        Log.i("sync_","Called before firestore"+dataString)
        return Result.success()

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