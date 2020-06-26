package com.example.forvenders.activity

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.forvenders.R
import com.example.forvenders.base.BaseActivity
import com.example.forvenders.base.PreferanceRepository
import com.example.forvenders.databinding.ActivityDashboardBinding
import com.facebook.login.LoginManager
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : BaseActivity<ActivityDashboardBinding>(){

    private lateinit var mBinding: ActivityDashboardBinding
    private var db: FirebaseFirestore? = null
    private var servicesName : ArrayList<String>? = null
    private var databaseRef: DatabaseReference? = null

    override fun onPermissionsGranted(requestCode: Int) {
    }

    override fun contentView() = R.layout.activity_dashboard

    override fun initUI(binding: ActivityDashboardBinding) {
        mBinding = binding

        db = FirebaseFirestore.getInstance()


        mBinding.toolBar.imgBack.setOnClickListener {
            onBackPressed()
        }

        mBinding.toolBar.txtSignOut.setOnClickListener {
            mAuth.signOut()
            LoginManager.getInstance().logOut()
            PreferanceRepository.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        servicesName = ArrayList()
        mBinding.progressBar.visibility = View.VISIBLE
        databaseRef = FirebaseDatabase.getInstance().getReference("Services")
        databaseRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                mBinding.progressBar.visibility = View.GONE
            }
            override fun onDataChange(p0: DataSnapshot) {
                for(i in p0.children.iterator()){
                    servicesName?.add(i.getValue(String::class.java)!!)
                }
                mBinding.txtTotalServices.text = "Total Services: ${servicesName!!.size}"
                updateDropDown(servicesName!!)

                mBinding.progressBar.visibility = View.GONE
            }
        })


        mBinding.spServices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                if(servicesName!!.isNotEmpty()){
                    mBinding.txtHeading.text = servicesName!![position]
                    mBinding.txtRegister.text = "Do you want to Register ${servicesName!![position]} Services"
                }
            }
        }

        mBinding.btnRegister.setOnClickListener {
            startActivity(Intent(this, UserServiceRegistration::class.java). putExtra("service_for", mBinding.txtHeading.text.toString()))
        }

        mBinding.btnInfo.setOnClickListener {
            if(PreferanceRepository.getString("user_id")!=null && PreferanceRepository.getString("user_id").isNotEmpty()
                && PreferanceRepository.getString("service")!=null && PreferanceRepository.getString("service").isNotEmpty()){
                startActivity(Intent(this,  VenderInformationActivity::class.java).putExtra("service_for", PreferanceRepository.getString("service")))
            }else{
               showToast("Register First")
            }
        }

    }

    fun updateDropDown(servicesName: ArrayList<String>) {
        val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, R.layout.spinner_value, servicesName
        )
        mBinding.spServices.adapter = spinnerArrayAdapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finishAffinity()
    }

}