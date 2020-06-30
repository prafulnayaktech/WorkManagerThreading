package com.techolution.firestore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.work.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MetadataChanges
import com.techolution.firestore.databinding.ActivityNoteBinding
import com.techolution.firestore.services.*
import timber.log.Timber
import java.io.*
import java.util.*


class NoteActivity : AppCompatActivity() {
    lateinit var binding: ActivityNoteBinding
    private val REQUEST_CODE_IMAGE = 100
    private val REQUEST_CODE_PERMISSIONS = 101
    private val permissions = Arrays.asList(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var imageUri:Uri?= null
    private var pathI:String? = null
    private var permissionRequestCount: Int = 0
    private val MAX_NUMBER_REQUEST_PERMISSIONS = 2

    val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_note)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_note)
        val workManager = WorkManager.getInstance(application)

        val constraits = Constraints.Builder().setRequiresCharging(true).setRequiredNetworkType(NetworkType.CONNECTED)

        requestPermissionsIfNecessary()

        binding.selectImage.setOnClickListener {
            val chooseIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE)
        }

        binding.save.setOnClickListener {
            Log.i("thred_"," :"+Thread.currentThread().name)
            Toast.makeText(this,"Saved",Toast.LENGTH_LONG).show()
            val data = NoteData(binding.title.text.toString(), binding.desc.text.toString(), binding.other.text.toString(),pathI.toString())
            val syncBuilder = OneTimeWorkRequestBuilder<CloudWorker>()
            syncBuilder.setInputData(createInputData(data))
            syncBuilder.setConstraints(constraits.build())
            workManager.enqueue(syncBuilder.build())

        }

        binding.retrieve.setOnClickListener {
            retrieveData()
        }

        binding.insert.setOnClickListener {
            insert(binding.title.text.toString(), binding.desc.text.toString(), binding.other.text.toString())
        }

        binding.delete.setOnClickListener {
            deleteNote()
        }


        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        fireStore.firestoreSettings = settings



//        workManager.cancelAllWork()
    }

    private fun deleteNote() {
//        x5CU7yIzT0M4kVjWLsuE
        fireStore.collection("employee").document("gJYtIJEaBWwNDWqwx3cN")
            .delete()
            .addOnSuccessListener { Log.d("offline_delete", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("offline_delete", "Error deleting document", e) }

    }

    private fun insert(name:String, middle:String, last: String){
        val user: MutableMap<String, Any> = HashMap()
        user["first"] = name
        user["middle"] = middle
        user["last"] = last
        fireStore.enableNetwork().addOnSuccessListener {
            Log.w("online_", "enableNetwork Listen Success")
        }.addOnFailureListener {
            Log.w("online_", "enableNetwork Listen error",it)
        }
//         Add a new document with a generated ID
        fireStore.collection("employee")
                .add(user)
                .addOnSuccessListener { documentReference -> Log.d("offline_insert", "DocumentSnapshot added with ID: " + documentReference.id) }
                .addOnFailureListener { e -> Log.w("offline_insert", "Error adding document", e) }
    }

    private fun retrieveData(){
        fireStore.disableNetwork().addOnSuccessListener {
            Log.w("offline_", "Listen Success")
        }.addOnFailureListener {
            Log.w("offline_", "Listen error",it)
        }
        fireStore.collection("employee")
            .addSnapshotListener(MetadataChanges.INCLUDE ) { querySnapshot, e ->
                if (e != null) {
                    Log.w("offline_", "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d("offline_", "New Doc: ${change.document.data}")
                        Toast.makeText(this,"${change.document.id}",Toast.LENGTH_LONG).show()
                    }

                    val source = if (querySnapshot.metadata.isFromCache)
                        "local cache"
                    else
                        "server"
                    Log.d("offline_", "Data fetched from $source")
                }
            }
    }

    /** Permission Checking  */
    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestPermissionsIfNecessary() {

        if (!checkAllPermissions()) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                Toast.makeText(
                    this,
                    R.string.set_permissions_in_settings,
                    Toast.LENGTH_LONG
                ).show()
                binding.selectImage.isEnabled = false
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }

    /** Image Selection  */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> data?.let { handleImageRequestResult(data) }
                else -> Timber.d("Unknown request code.")
            }
        } else {
            Timber.e(String.format("Unexpected Result code %s", resultCode))
        }
    }

    @SuppressLint("LogNotTimber")
    private fun handleImageRequestResult(intent: Intent) {
        // If clipdata is available, we use it, otherwise we use data

        imageUri = intent.clipData?.getItemAt(0)?.uri ?: intent.data

        if (imageUri == null) {
            Timber.e("Invalid input image Uri.")
            return
        }else{
            Log.i("sync_","started")
//            binding.imageView.setImageURI(imageUri)
            val resolver = applicationContext.contentResolver
            pathI = saveToInternalStorage(BitmapFactory.decodeStream(resolver.openInputStream(imageUri!!)))
            Log.i("sync_","path ${pathI}")
            pathI?.let { loadImageFromStorage(it) }

        }

//        val filterIntent = Intent(this, NoteActivity::class.java)
//        filterIntent.putExtra(KEY_IMAGE_URI, imageUri.toString())
//        startActivity(filterIntent)
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "profile.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    private fun loadImageFromStorage(path: String) {
        try {
            val f = File(path, "profile.jpg")
            val b = BitmapFactory.decodeStream(FileInputStream(f))

            binding.imageView.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

}