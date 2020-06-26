package com.example.forvenders.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.forvenders.R
import com.example.forvenders.base.BaseActivity
import com.example.forvenders.base.PreferanceRepository
import com.example.forvenders.databinding.ActivityUserServiceRegistrationBinding
import com.example.forvenders.model.CarModel
import com.example.forvenders.model.MechanicModel
import com.facebook.login.LoginManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class UserServiceRegistration : BaseActivity<ActivityUserServiceRegistrationBinding>() {

    private lateinit var mBinding: ActivityUserServiceRegistrationBinding
    private var serviceFor = ""
    private var carModel: CarModel? = null
    private var mechModel: MechanicModel? = null
    private var db: FirebaseFirestore? = FirebaseFirestore.getInstance()
    private var alertDialog: AlertDialog? = null
    private var profilePhoto: Uri? = null
    private var mStorageRef: StorageReference? = null

    override fun onPermissionsGranted(requestCode: Int) {
        when (requestCode) {
            100 -> takeCamera()
        }
    }

    override fun contentView() = R.layout.activity_user_service_registration

    override fun initUI(binding: ActivityUserServiceRegistrationBinding) {
        mBinding = binding
        mStorageRef = FirebaseStorage.getInstance().reference

        serviceFor = intent.getStringExtra("service_for")!!

        if(serviceFor == "Mechanic"){
            mBinding.llMechanic.visibility = View.VISIBLE
            mBinding.llCar.visibility = View.GONE
        }else{
            mBinding.llMechanic.visibility = View.GONE
            mBinding.llCar.visibility = View.VISIBLE

        }

        mBinding.txtRegistration.text = "Registration Form for $serviceFor"

        mBinding.toolBar.imgBack.setOnClickListener {
            finish()
        }

        mBinding.toolBar.txtSignOut.setOnClickListener {
            mAuth.signOut()
            LoginManager.getInstance().logOut()
            PreferanceRepository.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        mBinding.btnRegister.setOnClickListener {
            if (validationForCar()) {
                carModel = CarModel(
                    image = "",
                    id = mBinding.edtId.text.toString(),
                    name = mBinding.edtName.text.toString(),
                    identity = mBinding.edtIdentity.text.toString(),
                    vehicleName = mBinding.edtCarName.text.toString(),
                    vehicleNumber = mBinding.edtCarNumber.text.toString()
                )
                registerUser()
            } else {
                showToast("Missing")
            }
        }

        mBinding.btnMechRegister.setOnClickListener {
            if (validationForMechanic()) {
                mechModel = MechanicModel(
                    image = "",
                    id = mBinding.edtMechId.text.toString(),
                    name = mBinding.edtMechName.text.toString(),
                    identity = mBinding.edtMechIdentity.text.toString()
                )
                registerUserForMech()
            } else {
                showToast("Missing")
            }
        }
        mBinding.userImage.setOnClickListener {
            uploadImage()
        }
    }

    private fun registerUserForMech() {
        mBinding.progressBar.visibility = View.VISIBLE
        if (profilePhoto != null) {
            mStorageRef?.child("Users")?.child("${mAuth.currentUser?.uid}For$serviceFor")?.child("profilePic")
                ?.putFile(profilePhoto!!)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        mStorageRef?.child("Users")?.child("${mAuth.currentUser?.uid}For$serviceFor")?.child("profilePic")
                            ?.downloadUrl?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                PreferanceRepository.setString("mech_url", it.result.toString())
                                mechModel?.image = it.result.toString()
                                uploadDataForMech(mechModel)
                            }
                        }?.addOnFailureListener {
                            uploadDataForMech(mechModel)
                        }
                    }
                }?.addOnFailureListener {
                    uploadDataForMech(mechModel)
                }
        } else {
            uploadDataForMech(mechModel)
        }
    }

    private fun registerUser() {
        mBinding.progressBar.visibility = View.VISIBLE

        if (profilePhoto != null) {
            mStorageRef?.child("Users")?.child("${mAuth.currentUser?.uid}For$serviceFor")?.child("profilePic")
                ?.putFile(profilePhoto!!)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        mStorageRef?.child("Users")?.child("${mAuth.currentUser?.uid}For$serviceFor")?.child("profilePic")
                            ?.downloadUrl?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                PreferanceRepository.setString("car_url", it.result.toString())
                                carModel?.image = it.result.toString()
                                uploadData(carModel)
                            }
                        }?.addOnFailureListener {
                            uploadData(carModel)
                        }
                    }
                }?.addOnFailureListener {
                    uploadData(carModel)
                }
        } else {
            uploadData(carModel)
        }
    }

    private fun uploadData(carModel: CarModel?) {
        db?.collection("Users")?.document("${mAuth.currentUser?.uid}For$serviceFor")?.set(carModel!!)
            ?.addOnSuccessListener {
                showToast("Register Done")
                PreferanceRepository.setString("user_id", mBinding.edtId.text.toString())
                PreferanceRepository.setString("service", serviceFor)
                mBinding.progressBar.visibility = View.GONE
                val intent = Intent(this, VenderInformationActivity::class.java)
                intent.putExtra("service_for", serviceFor)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }?.addOnFailureListener {
                showToast("Register Failed ${it.message}")
                mBinding.progressBar.visibility = View.GONE
            }
    }

    private fun uploadDataForMech(mechModel: MechanicModel?) {
        db?.collection("Users")?.document("${mAuth.currentUser?.uid}For$serviceFor")?.set(mechModel!!)
            ?.addOnSuccessListener {
                showToast("Register Done")
                PreferanceRepository.setString("user_id", mBinding.edtMechId.text.toString())
                PreferanceRepository.setString("service", serviceFor)
                mBinding.progressBar.visibility = View.GONE
                val intent = Intent(this, VenderInformationActivity::class.java)
                intent.putExtra("service_for", serviceFor)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }?.addOnFailureListener {
                showToast("Register Failed ${it.message}")
                mBinding.progressBar.visibility = View.GONE
            }
    }

    private fun uploadImage() {
        val dialogBuilder = AlertDialog.Builder(this)
        val layoutView: View = layoutInflater.inflate(R.layout.dialog_file_upload, null)
        val takeCamera: TextView = layoutView.findViewById(R.id.txt_take_pic)
        dialogBuilder.setView(layoutView)
        alertDialog = dialogBuilder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()

        takeCamera.setOnClickListener {
            requestAppPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), R.string.permission_text, 100
            )
        }
    }

    private fun takeCamera() {
        alertDialog?.dismiss()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, 100)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    val imgInBitmapDrawable = data!!.extras!!["data"] as Bitmap?

                    val bytes = ByteArrayOutputStream()
                    imgInBitmapDrawable?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    val path = MediaStore.Images.Media.insertImage(
                        contentResolver,
                        imgInBitmapDrawable,
                        "ProfilePic",
                        null
                    )
                    profilePhoto = Uri.parse(path.toString())
                    Glide.with(this).load(profilePhoto).into(mBinding.userImage)
                }
            }
        }
    }

    private fun validationForCar(): Boolean {
        when {
            mBinding.edtId.text.isEmpty() -> {
                mBinding.edtId.error = "Enter Id"
                return false
            }
            mBinding.edtName.text.isEmpty() -> {
                mBinding.edtName.error = "Enter Name"
                return false
            }
            mBinding.edtIdentity.text.isEmpty() -> {
                mBinding.edtIdentity.error = "Enter identity"
                return false
            }
            mBinding.edtCarName.text.isEmpty() -> {
                mBinding.edtCarName.error = "Enter Vehicle Name"
                return false
            }
            mBinding.edtCarNumber.text.isEmpty() -> {
                mBinding.edtCarNumber.error = "Enter Vehicle Number"
                return false
            }
            else -> return true
        }
    }

    private fun validationForMechanic(): Boolean {
        return when {
            mBinding.edtMechId.text.isEmpty() -> {
                mBinding.edtMechId.error = "Enter Id"
                false
            }
            mBinding.edtMechName.text.isEmpty() -> {
                mBinding.edtMechName.error = "Enter Name"
                false
            }
            mBinding.edtMechIdentity.text.isEmpty() -> {
                mBinding.edtMechIdentity.error = "Enter identity"
                false
            }
            else -> true
        }
    }
}