package com.example.forvenders.activity

import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.forvenders.R
import com.example.forvenders.adapter.SubServicesAdapter
import com.example.forvenders.base.BaseActivity
import com.example.forvenders.base.PreferanceRepository
import com.example.forvenders.databinding.ActivityVenderInfoBinding
import com.example.forvenders.model.ServicesModel
import com.facebook.login.Login
import com.facebook.login.LoginManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class VenderInformationActivity : BaseActivity<ActivityVenderInfoBinding>(){

    private lateinit var mBinding: ActivityVenderInfoBinding
    private var serviceFor = ""
    private var db: FirebaseFirestore? = null
    private var servicesModel = ServicesModel()
    private var name : ArrayList<String> = ArrayList()
    private var serviceAdapter : SubServicesAdapter? = null

    override fun onPermissionsGranted(requestCode: Int) {
    }
    override fun contentView() = R.layout.activity_vender_info

    override fun initUI(binding: ActivityVenderInfoBinding) {
        mBinding = binding
        db = FirebaseFirestore.getInstance()

        serviceFor = intent.getStringExtra("service_for")!!
        serviceAdapter = SubServicesAdapter()
        mBinding.rvInfo.adapter = serviceAdapter

        val requestOptions: RequestOptions by lazy {
            RequestOptions()
                .placeholder(R.drawable.ic_place_holder)
                .transforms(CenterCrop())
        }

        if(serviceFor == "Mechanic"){
            Glide.with(this).load(PreferanceRepository.getString("mech_url")).apply(requestOptions).into(mBinding.userImage)
        }else{
            Glide.with(this).load(PreferanceRepository.getString("car_url")).apply(requestOptions).into(mBinding.userImage)
        }

        fetchForRv()

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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("service_for", serviceFor)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun fetchForRv() {
        mBinding.progressBar.visibility = View.VISIBLE
        db?.collection("Admin")?.document(serviceFor)?.get()?.addOnCompleteListener {
            if(it.isSuccessful){
                servicesModel = it.result?.toObject(ServicesModel::class.java)!!
                name.clear()
                servicesModel.name?.map { it ->
                    it.mapValues {
                        name.add(it.value)
                    }
                }
                serviceAdapter?.addItem(name)
                serviceAdapter?.notifyDataSetChanged()
                mBinding.progressBar.visibility = View.GONE

            } else{
                showToast("Something went wrong")
                mBinding.progressBar.visibility = View.GONE
            }
        }?.addOnFailureListener {
            showToast(it.message.toString())
            mBinding.progressBar.visibility = View.GONE
        }
    }

}