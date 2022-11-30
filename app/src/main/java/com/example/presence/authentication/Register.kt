package com.example.presence.authentication

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.presence.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import com.example.presence.data.Streak
import com.example.presence.data.User
import com.example.presence.data.Check
import kotlinx.android.synthetic.main.activity_register.*


@Suppress("DEPRECATION")
class Register : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var detector: FaceDetector
    private lateinit var streak:Streak
    private lateinit var bounce: Animation
    private var MAGIC_CLOAK : Long = 1500

    private companion object{
        private const val SCALING_FACTOR = 4
        private const val TAG = "FACE_DETECT_TAG"
    }
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        bounce = AnimationUtils.loadAnimation(
            this, R.anim.bounce)

        val scroll=findViewById<ScrollView>(R.id.scrollView)
        ObjectAnimator.ofInt(scroll,"scrollY",0,1900).apply {
            duration=1000
            startDelay=500
        }.start()

        Handler(Looper.myLooper()!!).postDelayed({
            findViewById<TextView>(R.id.magic_cloak0).visibility=View.GONE
            findViewById<TextView>(R.id.magic_cloak1).visibility=View.GONE
            findViewById<TextView>(R.id.magic_cloak2).visibility=View.GONE
            findViewById<TextView>(R.id.magic_cloak3).visibility=View.GONE
            findViewById<TextView>(R.id.magic_cloak4).visibility=View.GONE
            findViewById<TextView>(R.id.magic_cloak5).visibility=View.GONE
            findViewById<TextView>(R.id.magic_cloak6).visibility=View.GONE
        },MAGIC_CLOAK)

        // High-accuracy landmark detection and face classification
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        detector = FaceDetection.getClient(highAccuracyOpts)
        // Real-time contour detection
        /*val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        detector = FaceDetection.getClient(realTimeFdo)*/
        val dropDown : Spinner = findViewById(R.id.gender)

        val listed  = ArrayList<String>()
        listed.add("Male")
        listed.add("Female")

        val listedAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,listed)

        dropDown.setAdapter(listedAdapter)

        val dropDown1 : Spinner = findViewById(R.id.designation)

        val listed1  = ArrayList<String>()
        listed1.add("Employee")
        var databaseReference: DatabaseReference=FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        databaseReference.child("admin").get().addOnSuccessListener {
            if(!it.exists())
            {
                listed1.add("Employer")
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        val listedAdapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,listed1)

        dropDown1.setAdapter(listedAdapter1)

        val textView = findViewById<TextView>(R.id.alreadyHaveAccount)
        textView.setOnClickListener{
            val intent = Intent(this , LoginActivity :: class.java)
            startActivity(intent)

            overridePendingTransition(
                androidx.appcompat.R.anim.abc_slide_in_top,
                androidx.appcompat.R.anim.abc_slide_out_bottom)
            finish()
        }
        val image=findViewById<ImageView>(R.id.profile)
        image.setOnClickListener{
            selectImage()
        }
        val button=findViewById<Button>(R.id.register)
        button.setOnClickListener{
            button.startAnimation(bounce)
            var count=0
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.fullName).text.toString())){
                count+=1
                Toast.makeText(this@Register, "Full name required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<Spinner>(R.id.gender).selectedItem.toString())){
                count+=1
                Toast.makeText(this@Register, "Gender required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.employeeNo).text.toString())){
                count+=1
                Toast.makeText(this@Register, "Employee number required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.phoneNo).text.toString())){
                count+=1
                Toast.makeText(this@Register, "Phone number required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.email).text.toString())){
                count+=1
                Toast.makeText(this@Register, "Email required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.officeAddress).text.toString())){
                count+=1
                Toast.makeText(this@Register, "Office Address required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.password).text.toString())){
                count+=1
                Toast.makeText(this@Register, "Password required", Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<Spinner>(R.id.designation).selectedItem.toString())){
                count+=1
                Toast.makeText(this@Register, "Designation required", Toast.LENGTH_SHORT).show()
            }
            if(count==0)
            {
                val fullName=findViewById<EditText>(R.id.fullName).text.toString()
                val gender=findViewById<Spinner>(R.id.gender).selectedItem.toString()
                val employeeNo=findViewById<EditText>(R.id.employeeNo).text.toString()
                val phoneNo=findViewById<EditText>(R.id.phoneNo).text.toString()
                val email=findViewById<EditText>(R.id.email).text.toString()
                val officeAddress=findViewById<EditText>(R.id.officeAddress).text.toString()
                val password=findViewById<EditText>(R.id.password).text.toString()
                val designation=findViewById<Spinner>(R.id.designation).selectedItem.toString()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        databaseReference= FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
                        val user = User(fullName,gender,employeeNo,phoneNo,officeAddress,designation)
                        streak=Streak(employeeNo,fullName,"0","0","0",firebaseUser.uid,"No")
                        if(designation=="Employer")
                        {
                            streak=Streak(employeeNo,fullName,"0","0","0",firebaseUser.uid,"Yes")
                            val check=Check(firebaseUser.uid,email)
                            val ddd: DatabaseReference=FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
                            ddd.setValue(check).addOnSuccessListener {

                            }.addOnFailureListener {

                            }
                        }
                        val db=FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Employer")
                        db.child(firebaseUser.uid).setValue(streak).addOnCompleteListener {
                            if(it.isSuccessful)
                            {
                                Toast.makeText(this@Register,"data uploaded",Toast.LENGTH_SHORT).show()
                            }
                            else
                            {
                                Toast.makeText(this@Register,"data not uploaded",Toast.LENGTH_SHORT).show()
                            }
                        }
                        uploadImage(firebaseUser.uid)

                        databaseReference.child(firebaseUser.uid).setValue(user).addOnCompleteListener{
                            if(it.isSuccessful)
                            {
                                Toast.makeText(this@Register,"data uploaded",Toast.LENGTH_SHORT).show()
                            }
                            else
                            {
                                Toast.makeText(this@Register,"data not uploaded",Toast.LENGTH_SHORT).show()
                            }
                        }
                        Toast.makeText(this@Register,"Registration Successful.",Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        val intent=Intent(this, LoginActivity::class.java)
                        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra("em",email)
                        intent.putExtra("ps",password)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        Toast.makeText(this@Register,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun selectImage() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),111)
        }
        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,1)
    }
    private fun uploadImage(uid:String?=null) {
        val storageReference= FirebaseStorage.getInstance("gs://presence-23cbb.appspot.com").getReference("Profile/$uid.jpg")
        val imageView=findViewById<ImageView>(R.id.profile)
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        storageReference.putBytes(data).addOnSuccessListener {
            Toast.makeText(this@Register,"Image Uploaded",Toast.LENGTH_SHORT).show()
            findViewById<ImageView>(R.id.profile).setImageURI(null)
        }.addOnFailureListener {
            Toast.makeText(this@Register,"Image not uploaded",Toast.LENGTH_SHORT).show()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1 && resultCode== Activity.RESULT_OK)
        {
            var pic: Bitmap? =data?.getParcelableExtra("data")
            if (pic != null) {
                analyzePhoto(pic)
            }
        }

    }
    private fun analyzePhoto(bitmap: Bitmap){
        Log.d(TAG, "analyzePhoto :")
        val smallerBitmap = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width / SCALING_FACTOR,
            bitmap.height / SCALING_FACTOR,
            false
        )
        val inputImage = InputImage.fromBitmap(smallerBitmap, 0)
        detector.process(inputImage)
            .addOnSuccessListener { faces->
                Log.d(TAG, "analyzePhoto :Successfully detected face...")
                Toast.makeText(this , "Face Detected", Toast.LENGTH_SHORT).show()

                for(face in faces){
                    val rect = face.boundingBox
                    rect.set(
                        rect.left* SCALING_FACTOR +5,
                        rect.top * (SCALING_FACTOR) ,
                        rect.right * (SCALING_FACTOR) +7,
                        rect.bottom * SCALING_FACTOR + 12
                    )
                }

                cropDetectedFace(bitmap, faces)
            }
            .addOnFailureListener { e->
                Log.e(TAG,"analyzePhoto :",e )
                Toast.makeText(this , "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cropDetectedFace(bitmap: Bitmap, faces:List<Face>){
        Log.d(TAG, "cropFetectedFace :")

        val rect = faces[0].boundingBox
        val x = Math.max(rect.left, 0)
        val y = Math.max(rect.top, 0)

        val width = rect.width()
        val height = rect.height()

        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            x,
            y,
            if(x + width > bitmap.width) bitmap.width - x else width,
            if (y + height > bitmap.height) bitmap.height - y else height
        )
        findViewById<ImageView>(R.id.profile).setImageBitmap(croppedBitmap)
    }
}

