package com.example.forvenders.activity

import android.content.Intent
import com.example.forvenders.R
import com.example.forvenders.adapter.SlidingImageAdapter
import com.example.forvenders.base.BaseActivity
import com.example.forvenders.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity<ActivitySplashBinding>(){

    private lateinit var mBinding: ActivitySplashBinding
    var pager: AutoScrollViewPager? = null

    override fun contentView(): Int  = R.layout.activity_splash

    override fun onPermissionsGranted(requestCode: Int) {
    }

    override fun initUI(binding: ActivitySplashBinding) {
        mBinding = binding

        mBinding.btnStart.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        initializePager()
    }

    private fun initializePager() {
        val images = ArrayList<String>()
        images.add("https://i.pinimg.com/originals/f1/7c/b2/f17cb2f3bd0564e1b52e4d1817c3f412.jpg")
        images.add("https://www.itl.cat/pngfile/big/14-146143_beautiful-mobile-photo-pics-pictures-wallpaper-hd-beautiful.jpg")
        images.add("https://www.itl.cat/pngfile/big/8-80807_beautiful-mobile-photo-wallpaper-pictures-pics-hd-download.jpg")

        pager = mBinding.pager
        pager?.adapter = SlidingImageAdapter(this, images)
        pager!!.currentItem = 0
        pager?.isCycle = true
        pager?.startAutoScroll()
        pager?.interval = 5000
        pager?.direction = AutoScrollViewPager.RIGHT
    }
}