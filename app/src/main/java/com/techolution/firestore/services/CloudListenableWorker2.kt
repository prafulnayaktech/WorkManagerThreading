package com.techolution.firestore.services

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture

class CloudListenableWorker2(context: Context,workerParameters: WorkerParameters):ListenableWorker(context,workerParameters) {
    override fun startWork(): ListenableFuture<Result> {
        TODO("Not yet implemented")
    }
}