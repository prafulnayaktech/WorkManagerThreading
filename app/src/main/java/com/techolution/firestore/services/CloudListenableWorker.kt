package com.techolution.firestore.services

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.Call
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.Response
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import javax.security.auth.callback.Callback


class CloudListenableWorker(context: Context, workerParameters: WorkerParameters): ListenableWorker(context, workerParameters) {
    val fireStore = FirebaseFirestore.getInstance()

    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
//        val future : ResolvableFuture<Result> = ResolvableFuture.create()
//        GlobalScope.launch {
//            //Work in a separated thread (running not blocking)
//            doAnyWork()
//        }

        return CallbackToFutureAdapter.getFuture{completer ->

        }


//        return CallbackToFutureAdapter.getFuture {completer ->
//            val runnablanleTask = Runnable {
//                Log.i("thred_dowork"," :"+Thread.currentThread().id)
//                val appContext = applicationContext
//                val dataString = inputData.getString(KEY_NOTE_DATA)
//                val data = Gson().fromJson(dataString, NoteData::class.java)
//                makeStatusNotification("Uploading Notes", appContext)
//                Log.i("uri:"," ${data.uri}")
//
//                val resolver = appContext.contentResolver
//
//                appContext.grantUriPermission(appContext.packageName,
//                    Uri.parse(data.uri),
//                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
//                )
//
//
//                val f = File(data.uri, "profile.jpg")
//                val picture = BitmapFactory.decodeStream(FileInputStream(f))
//                // Create a storage reference from our app
//                val storage = Firebase.storage
//                // Create a storage reference from our app
//                val storageRef = storage.reference
//
//                val baos = ByteArrayOutputStream()
//                picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                val imageDataX: ByteArray = baos.toByteArray()
//
//                var pathX = "images/"+System.currentTimeMillis().toString()+".jpg"
//                val mountainImagesRef = storageRef.child(pathX)
//                val uploadTask = mountainImagesRef.putBytes(imageDataX)
//
//                uploadTask.addOnSuccessListener {
//                    Log.i("sync_","Image uploaded"+ it.metadata!!.path+"   :  "+it.metadata!!.name)
//                    data.uri= it.metadata!!.path
//                    Log.i("sync_","upload Succes: thread_ "+Thread.currentThread().name)
//                    fireStore.collection("notes")
//                        .add(data)
//                        .addOnSuccessListener {
//                            Log.i("sync_","Sync Succes: thread_ "+Thread.currentThread().name)
//                            if(isStopped)
//                                Log.i("sync_","Worker Stopped")
//                            else
//                                saveUrl(completer)
//
//                        }.addOnFailureListener {
//                            Log.i("sync_","Sync Failed")
//                            completer.set(Result.failure())
//                        }
//                }.addOnFailureListener{
//                    Log.e("sync_","Image not uploaded"+it.toString())
//                    completer.set(Result.failure())
//                }
//                Timber.i("sync_data: $dataString")
//
//                Log.i("sync_","Called before firestore"+dataString)
//            }
//            backgroundExecutor.execute(runnablanleTask)
////            GlobalScope.launch {
////
////            }
//
//        }
    }

    private suspend fun fetchDoc() {


    }

    private fun doAnyWork() {

    }

    private fun saveUrl(completer: Completer<Result>) {

        Log.i("thread_ync_","sleeping for 15 sec. thread_ "+Thread.currentThread().name)
        Thread.sleep(15000)
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
                    completer.set(Result.success())
                } else {
                    Log.w("sync_", "Error getting documents.", task.exception)
                    completer.set(Result.failure())
                }
            }
    }
}