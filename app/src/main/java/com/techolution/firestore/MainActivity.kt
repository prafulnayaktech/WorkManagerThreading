package com.techolution.firestore

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.work.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.techolution.firestore.services.WorkerSync
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = FirebaseFirestore.getInstance()
//        db.collection("Users").get().addOnCompleteListener(OnCompleteListener {
//            if(it.isSuccessful)
//                Log.e("successful",""+it.result.toString())
//            else
//                Log.e("error",""+it.toString())
//
//        })
        val workManager = WorkManager.getInstance(application)
//        val syncW = OneTimeWorkRequestBuilder<WorkerSync>().build()

        val myUploadWork = PeriodicWorkRequestBuilder<WorkerSync>(
            15, TimeUnit.MINUTES, // repeatInterval (the period cycle)
            1, TimeUnit.MINUTES) // flexInterval
            .build()
        val myWorkRequest = OneTimeWorkRequestBuilder<WorkerSync>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()
//        val blurRequest = OneTimeWorkRequestBuilder<WorkerSync>()
//            .setInputData(createInputDataForUri())
//            .build()
//        workManager.beginWith(syncW)
        workManager.enqueue(myWorkRequest)

//        workManager.enqueue(sycWorker)
//        workManager.enqueue(sycWorker)
        // Create a new user with a first, middle, and last name

        // Create a new user with a first, middle, and last name
        val user: MutableMap<String, Any> = HashMap()
        user["first"] = "Alan"
        user["middle"] = "Mathison"
        user["last"] = "Turing"
        user["born"] = 1912

// Add a new document with a generated ID

//        var continuation = workManager.beginWith(OneTimeWorkRequest.from(CleanupWorker::class.java))
//        val blurRequest=
//                OneTimeWorkRequestBuilder<BlurWorker>().setInputData(createInputDataForUri()).build()

// Add a new document with a generated ID
//        db.collection("employee")
//                .add(user)
//                .addOnSuccessListener { documentReference -> Log.d("this", "DocumentSnapshot added with ID: " + documentReference.id) }
//                .addOnFailureListener { e -> Log.w("this", "Error adding document", e) }


        db.collection("employee")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d("FragmentActivity.TAG", document.id + " => " + document.data)
                        }
                    } else {
                        Log.w("FragmentActivity.TAG", "Error getting documents.", task.exception)
                    }
                }
    }


}