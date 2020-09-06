//package com.prasunmondal.ah.master.timetrack
//
//import android.util.Log
//import com.prasunmondal.ah.master.timetrack.sessionData.AppContexts
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileWriter
//import java.io.InputStreamReader

//import android.Manifest
//import android.content.pm.PackageManager
//import androidx.appcompat.app.AppCompatActivity
//import com.prasunmondal.ah.master.timetrack.Utils.*
//import com.google.android.material.snackbar.Snackbar
//import kotlinx.android.synthetic.main.activity_main.*
//
//fun storagePermission() {
//    val permissions = Permissions();
//    permissions.storagePermission()
//}
//
//class Permissions : AppCompatActivity() {
//
//    fun storagePermission() {
//        checkStoragePermission()
//    }
//
//    private fun checkStoragePermission() {
//        // Check if the storage permission has been granted
//        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//            requestStoragePermission()
//    }
//
//    private fun requestStoragePermission() {
//        val PERMISSION_REQUEST_STORAGE = 0
//        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            mainLayout.showSnackbar(
//                R.string.storage_access_required,
//                Snackbar.LENGTH_INDEFINITE, R.string.ok
//            ) {
//                requestPermissionsCompat(
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    PERMISSION_REQUEST_STORAGE
//                )
//            }
//
//        } else {
//            requestPermissionsCompat(
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                PERMISSION_REQUEST_STORAGE
//            )
//        }
//    }
//}