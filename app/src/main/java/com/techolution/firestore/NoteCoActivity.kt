package com.techolution.firestore

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.techolution.firestore.databinding.ActivityNoteBinding
import com.techolution.firestore.services.CloudWorker
import com.techolution.firestore.services.NoteData
import com.techolution.firestore.services.createInputData
import kotlinx.android.synthetic.main.activity_note.*

class NoteCoActivity: AppCompatActivity() {
    lateinit var binding: ActivityNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_note)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_note)
        val workManager = WorkManager.getInstance(application)

        val constraits = Constraints.Builder().setRequiresCharging(true).setRequiredNetworkType(
            NetworkType.CONNECTED)
        binding.save.setOnClickListener {
            Toast.makeText(this,"Saved", Toast.LENGTH_LONG).show()
            val data = NoteData(binding.title.text.toString(), binding.desc.text.toString(), binding.other.text.toString(), "asd")
            val syncBuilder = OneTimeWorkRequestBuilder<CloudWorker>()
            syncBuilder.setInputData(createInputData(data))
            syncBuilder.setConstraints(constraits.build())
            workManager.enqueue(syncBuilder.build())
        }

//        workManager.cancelAllWork()
    }

}