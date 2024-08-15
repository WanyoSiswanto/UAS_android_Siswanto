package com.siswanto.parkirliar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var photoUri: Uri? = null
    private lateinit var imageView: ImageView // ImageView untuk menampilkan foto
    private lateinit var coordinateTextView: TextView // TextView untuk menampilkan koordinat

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Mengambil data intent dari MainActivity
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // Inisialisasi Firebase Database dan Storage
        database = FirebaseDatabase.getInstance("https://uasandroid-56802-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        storageReference = FirebaseStorage.getInstance().reference

        val locationInput = findViewById<EditText>(R.id.locationInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        imageView = findViewById(R.id.capturedImageView)
        coordinateTextView = findViewById(R.id.coordinateTextView)

        // Menampilkan koordinat pada TextView
        coordinateTextView.text = "Lat: $latitude, Long: $longitude"

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            openCamera()
        }

        findViewById<Button>(R.id.submitReportButton).setOnClickListener {
            val location = locationInput.text.toString()
            val description = descriptionInput.text.toString()

            if (location.isBlank() || description.isBlank() || photoUri == null) {
                Toast.makeText(this, "Please fill in all fields and take a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadPhoto { imageUrl ->
                saveReportToFirebase(location, description, latitude, longitude, imageUrl)
            }
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "No Camera app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveReportToFirebase(location: String, description: String, latitude: Double, longitude: Double, imageUrl: String) {
        val reportId = database.push().key
        val report = Report(location, description, latitude, longitude, imageUrl)

        reportId?.let {
            database.child("reports").child(it).setValue(report)
                .addOnSuccessListener {
                    Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(photo) // Tampilkan foto di ImageView

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_$timeStamp.jpg"
            val imageRef = storageReference.child("images/$imageFileName")

            val baos = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val dataBytes = baos.toByteArray()

            val uploadTask = imageRef.putBytes(dataBytes)
            uploadTask.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    photoUri = uri
                }
            }
        }
    }

    private fun uploadPhoto(onComplete: (String) -> Unit) {
        photoUri?.let {
            onComplete(it.toString())
        } ?: onComplete("No Image")
    }
}
