package com.example.presence.face_recognization

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Size
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.presence.*
import com.example.presence.authentication.LoginActivity
import com.example.presence.bottom_navigation.Home
import com.example.presence.common.Task
import com.example.presence.model.FaceNetModel
import com.example.presence.model.Models
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.add_task_dialog.*
import kotlinx.android.synthetic.main.add_task_dialog.dialog_add
import kotlinx.android.synthetic.main.add_task_dialog.dialog_cancel
import kotlinx.android.synthetic.main.alert.*
import kotlinx.android.synthetic.main.mark_attendance_dialog_box.*
import java.io.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.concurrent.Executors

private var mContext: Context? = null
class Face : AppCompatActivity() {

    private var isSerializedDataStored = false

    // Serialized data will be stored ( in app's private storage ) with this filename.
    private val SERIALIZED_DATA_FILENAME = "image_data"

    // Shared Pref key to check if the data was stored.
    private val SHARED_PREF_IS_DATA_STORED_KEY = "is_data_stored"

    private lateinit var previewView : PreviewView
    private lateinit var frameAnalyser  : FrameAnalyser
    private lateinit var faceNetModel : FaceNetModel
    private lateinit var fileReader : FileReader
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var sharedPreferences: SharedPreferences

    // <----------------------- User controls --------------------------->

    // Use the device's GPU to perform faster computations.
    // Refer https://www.tensorflow.org/lite/performance/gpu
    private val useGpu = false

    // Use XNNPack to accelerate inference.
    // Refer https://blog.tensorflow.org/2020/07/accelerating-tensorflow-lite-xnnpack-integration.html
    private val useXNNPack =false

    // You may the change the models here.
    // Use the model configs in Models.kt
    // Default is Models.FACENET ; Quantized models are faster
    private val modelInfo = Models.FACENET
    // <---------------------------------------------------------------->
    companion object {
        lateinit var logTextView : TextView
        private lateinit var context: Context
        private var s:String="Null"
        private var currentLocation : Location?=null
        private lateinit var fusedLocationClient : FusedLocationProviderClient
        private val permissionCode = 101
        fun setMessage( message : String ) {
            logTextView.text = message
        }
        fun proxy()
        {
            val dialog = Dialog(mContext!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.alert)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            dialog.header.setText("Proxy Detected")
            dialog.text.setText("You are marking the proxy of the another person which is not allowed")

            dialog.dialog_add.setOnClickListener {
                dialog.dismiss()
                context.startActivity(Intent(context, LoginActivity::class.java))

            }
        }
        fun check() {
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")
            databaseReference.child("${FirebaseAuth.getInstance().currentUser?.uid}").get().addOnSuccessListener {
                if(it.exists())
                {
                    var fullName="${it.child("fullName").value}"
                    var gender="${it.child("gender").value}"
                    var employeeNo="${it.child("employeeNo").value}"
                    var phoneNo="${it.child("phoneNo").value}"
                    var officeAddress="${it.child("officeAddress").value}"
                    var designation="${it.child("designation").value}"
                    var formater= DateTimeFormatter.ISO_LOCAL_TIME
                    val time = HashMap<String, Any>()
                    var current=LocalDateTime.now()
                    var year=current.get(ChronoField.YEAR)
                    var month=current.get(ChronoField.MONTH_OF_YEAR)
                    var day=current.get(ChronoField.DAY_OF_MONTH)

                    val db=Firebase.firestore
                    val d=db.collection("User").document("Attendance").collection("${FirebaseAuth.getInstance().currentUser?.uid}").document(
                        year.toString()).collection(month.toString()).document(day.toString())


                    val dialog = Dialog(mContext!!)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.mark_attendance_dialog_box)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    dialog.dialog_cancel.setOnClickListener {
                        dialog.dismiss()
                        time["Exit"]=LocalTime.now().format(formater)
                        d.update(time).addOnSuccessListener {
                            Toast.makeText(context, "Exit time marked successfully", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {
                                exception: Exception -> Toast.makeText(context, exception.toString(), Toast.LENGTH_LONG).show()
                        }
                        val intent=Intent(context, Home::class.java)
                        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)

                    }

                    dialog.dialog_add.setOnClickListener{
                        dialog.dismiss()
                        time["IdName"]="$employeeNo-$fullName"
                        time["Date"]="$day/$month/$year"
                        time["Entry"]=LocalTime.now().format(formater)
                        time["GPS"]= s
                        d.set(time).addOnSuccessListener {
                            Toast.makeText(context, "Entry time marked successfully", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {
                                exception: Exception -> Toast.makeText(context, exception.toString(), Toast.LENGTH_LONG).show()
                        }
                        val intent=Intent(context, Home::class.java)
                        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)

                    }
                }
                else
                {
                    Toast.makeText(context, "User Doesn't exists", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load the data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext=this@Face
        // Remove the status bar to have a full screen experience
        // See this answer on SO -> https://stackoverflow.com/a/68152688/10878733
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!
                .hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        setContentView(R.layout.face)
        val livenessLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    context = this@Face
                    previewView = findViewById(R.id.preview_view)
                    logTextView = findViewById(R.id.log_textview)
                    logTextView.movementMethod = ScrollingMovementMethod()
                    run()
                }
            }
        livenessLauncher.launch(Intent(this, Liveness::class.java))
        // Implementation of CameraX preview


    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun run()
    {
        // Necessary to keep the Overlay above the PreviewView so that the boxes are visible.
        val boundingBoxOverlay = findViewById<BoundingBoxOverlay>(R.id.bbox_overlay)
        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)

        faceNetModel = FaceNetModel(this, modelInfo, useGpu, useXNNPack)
        frameAnalyser = FrameAnalyser(this, boundingBoxOverlay, faceNetModel)
        fileReader = FileReader(faceNetModel)
        // We'll only require the CAMERA permission from the user.
        // For scoped storage, particularly for accessing documents, we won't require WRITE_EXTERNAL_STORAGE or
        // READ_EXTERNAL_STORAGE permissions. See https://developer.android.com/training/data-storage
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
            startCameraPreview()
        }

        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        isSerializedDataStored = sharedPreferences.getBoolean(SHARED_PREF_IS_DATA_STORED_KEY, false)
        //if (!isSerializedDataStored) {
        //Logger.log("No serialized data was found. Select the images directory.")
        loadData()
        /*} else {
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Serialized Data")
                setMessage("Existing image data was found on this device.")
                setCancelable(false)
                setNegativeButton("LOAD") { dialog, which ->
                    dialog.dismiss()
                    frameAnalyser.faceList = loadSerializedImageData()
                    Logger.log("Serialized data loaded.")
                }
                create()
            }
            alertDialog.show()
        }*/
        fusedLocationClient =  LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
    }
    private fun loadData() {
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReference("Profile/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")
        val localFile= File.createTempFile("tempImage","jpg")
        storageReference.getFile(localFile).addOnSuccessListener{
            var bitmap= BitmapFactory.decodeFile(localFile.absolutePath)

            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")
            databaseReference.child("${FirebaseAuth.getInstance().currentUser?.uid}").get().addOnSuccessListener {
                if(it.exists())
                {
                    var fullName="${it.child("fullName").value}"

                    val images = ArrayList<Pair<String,Bitmap>>()
                    images.add(Pair(fullName, bitmap))
                    fileReader.run(images,fileReaderCallback)
                }
                else
                {
                    Toast.makeText(this@Face, "User Doesn't exists", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@Face, "Failed to load the data", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener{
            Toast.makeText(this@Face,"Failed to retrieve image", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------- //

    // Attach the camera stream to the PreviewView.
    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance( this )
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider) },
            ContextCompat.getMainExecutor(this) )
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()
        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing( CameraSelector.LENS_FACING_FRONT )
            .build()
        preview.setSurfaceProvider( previewView.surfaceProvider )
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size( 480, 640 ) )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser )
        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview , imageFrameAnalysis  )
    }

    // We let the system handle the requestCode. This doesn't require onRequestPermissionsResult and
    // hence makes the code cleaner.
    // See the official docs -> https://developer.android.com/training/permissions/requesting#request-permission
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch( Manifest.permission.CAMERA )
    }

    private val cameraPermissionLauncher = registerForActivityResult( ActivityResultContracts.RequestPermission() ) {
            isGranted ->
        if ( isGranted ) {
            startCameraPreview()
        }
        else {
            val alertDialog = AlertDialog.Builder( this ).apply {
                setTitle( "Camera Permission")
                setMessage( "The app couldn't function without the camera permission." )
                setCancelable( false )
                setPositiveButton( "ALLOW" ) { dialog, which ->
                    dialog.dismiss()
                    requestCameraPermission()
                }
                setNegativeButton( "CLOSE" ) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                create()
            }
            alertDialog.show()
        }

    }



    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(data: ArrayList<Pair<String, FloatArray>>, numImagesWithNoFaces: Int) {
            frameAnalyser.faceList = data
            saveSerializedImageData( data )
            Logger.log("Images parsed. Found $numImagesWithNoFaces images with no faces.")
        }
    }


    private fun saveSerializedImageData(data : ArrayList<Pair<String,FloatArray>> ) {
        val serializedDataFile = File( filesDir , SERIALIZED_DATA_FILENAME )
        ObjectOutputStream( FileOutputStream( serializedDataFile )  ).apply {
            writeObject( data )
            flush()
            close()
        }
        sharedPreferences.edit().putBoolean( SHARED_PREF_IS_DATA_STORED_KEY , true ).apply()
    }
    private fun loadSerializedImageData() : ArrayList<Pair<String,FloatArray>> {
        val serializedDataFile = File( filesDir , SERIALIZED_DATA_FILENAME )
        val objectInputStream = ObjectInputStream( FileInputStream( serializedDataFile ) )
        val data = objectInputStream.readObject() as ArrayList<Pair<String,FloatArray>>
        objectInputStream.close()
        return data
    }
    private fun fetchLocation(){
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
        val task = fusedLocationClient.lastLocation
        task.addOnSuccessListener{location ->
            if (location != null){
                currentLocation = location
                s = currentLocation?.latitude.toString()+" , "+
                        currentLocation?.longitude.toString()
            }
        }
    }

}
