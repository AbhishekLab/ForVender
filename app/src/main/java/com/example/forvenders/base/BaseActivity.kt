package com.example.forvenders.base

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.forvenders.R
import com.example.forvenders.activity.RuntimePermissionsActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity<in T : ViewDataBinding> : RuntimePermissionsActivity(){

    private var progressDialog: ProgressBar? = null
    private var v: View? = null

    private lateinit var mBinding: T

    private var permissionAllowed = false

    var builder: AlertDialog.Builder? = null
    var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mBinding = DataBindingUtil.setContentView(this, contentView())
        initUI(mBinding)

    }

    abstract fun contentView(): Int
    abstract fun initUI(binding: T)

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    protected open fun fragmentTransaction(
        transactionType: Int,
        fragment: Fragment,
        container: Int,
        isAddToBackStack: Boolean,
        bundle: Bundle?
    ) {
        if (bundle != null) {
            fragment.arguments = bundle
        }

        val trans = supportFragmentManager.beginTransaction()
        when (transactionType) {
            ADD_FRAGMENT -> trans.add(container, fragment, fragment.javaClass.simpleName)
            REPLACE_FRAGMENT -> {
                trans.replace(container, fragment, fragment.javaClass.simpleName)
                if (isAddToBackStack) trans.addToBackStack(null)
            }
        }
        trans.commit()
    }

    protected open fun showSnackbar(message: String) {
        val view = findViewById<View>(R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            showNetworkDialog()
        }
    }

    fun showNetworkDialog() {
        if (dialog == null) {
            builder = AlertDialog.Builder(this)
            builder!!.setTitle("No Internet Connection")
            builder!!.setMessage("Please check your internet connection")
            builder!!.setCancelable(false)
            builder!!.setPositiveButton("OK") { dia, which ->
                dia.dismiss()
                dialog = null
            }
            dialog = builder!!.create()
        }
        if (!dialog!!.isShowing)
            dialog!!.show()

    }


    companion object {
        const val ADD_FRAGMENT = 0
        const val REPLACE_FRAGMENT = 1
        lateinit var mAuth : FirebaseAuth
    }
}