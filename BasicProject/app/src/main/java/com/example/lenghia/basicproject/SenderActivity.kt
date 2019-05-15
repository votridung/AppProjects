package com.example.lenghia.basicproject

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.facebook.*
import com.facebook.login.LoginResult
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_sender.*
import org.json.JSONObject
import java.net.URL
import java.util.*

class SenderActivity : AppCompatActivity() {

    internal var callbackManager: CallbackManager? = null


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender)

        login_button.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"))
        callbackManager = CallbackManager.Factory.create()

        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val mDialog = ProgressDialog(this@SenderActivity)
                mDialog.setMessage("Retrieving data...")
                mDialog.show()

                val accesstoken = loginResult.accessToken.token

                val request = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
                    mDialog.dismiss()
                    getData(`object`)
                }

                val parameters = Bundle()
                parameters.putString("fields", "id,email,brithday,friends")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                error.printStackTrace()
            }
        })

        if (AccessToken.getCurrentAccessToken() != null)
            txtEmail.text = AccessToken.getCurrentAccessToken().userId

        printKeyHash()

    }

    private fun printKeyHash() {


    }

    private fun getData(jsonObject: JSONObject?) {
        try {

            val profile_picture = URL("https://graph.facebook.com/" + `jsonObject`!!.getString("id") + "/picture?width=250&height=250")
            Picasso.with(this)
                    .load(profile_picture.toString())
                    .into(avatar)
            txtEmail.text = jsonObject.getString("email")
            txtBirthday.text = jsonObject.getString("birthday")
            txtFriends.text = jsonObject.getString("Friends" + jsonObject.getJSONObject("friends").getJSONObject("summary").getString("total_count"))


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
