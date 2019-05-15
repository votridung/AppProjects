package com.example.lenghia.basicproject

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.lenghia.basicproject.Common.Common
import com.example.lenghia.basicproject.Model.PlaceDetail
import com.example.lenghia.basicproject.Remote.IGoogleAPIService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_place.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPlace : AppCompatActivity() {

    internal lateinit var mService: IGoogleAPIService
    var mPlace: PlaceDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)

        mService = Common.googleAPIService

        place_name.text = ""
        place_address.text = ""
        place_open_hour.text = ""


        btn_show_on_map.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.result!!.url))
            startActivity(mapIntent)
        }

        //load photo of place
        if (Common.currentResult!!.photos != null && Common.currentResult!!.photos!!.size > 0)
            Picasso.with(this)
                    .load(getPhotoOfPlace(Common.currentResult!!.photos!![0].photo_reference!!, 1000))
                    .into(photo)

        //load rating
        if (Common.currentResult!!.rating != null)
            rating_bar.rating = Common.currentResult!!.rating.toFloat()
        else
            rating_bar.visibility = View.GONE

        //load open hours
        if (Common.currentResult!!.opening_hours != null)
            place_open_hour.text = " Open now : " + Common.currentResult!!.opening_hours!!.open_now
        else
            place_open_hour.visibility = View.GONE

        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.place_id!!))
                .enqueue(object : retrofit2.Callback<PlaceDetail> {
                    override fun onResponse(call: Call<PlaceDetail>?, response: Response<PlaceDetail>?) {
                        mPlace = response!!.body()

                        place_address.text = mPlace!!.result!!.formatted_address
                        place_name.text = mPlace!!.result!!.name

                    }

                    override fun onFailure(call: Call<PlaceDetail>?, t: Throwable?) {
                        Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
                    }

                })
    }

    private fun getPlaceDetailUrl(place_id: String?): String {
        var url  = StringBuffer("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?placeid=$place_id")
        url.append("&key=AIzaSyD9td1ZlCRuB_DFcbvrfBzapPrylnOAjMI")
        return url.toString()
    }

    private fun getPhotoOfPlace(photo_reference: String, maxWidth: Int): String {
        var url  = StringBuffer("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photo_reference=$photo_reference")
        url.append("&key=AIzaSyD9td1ZlCRuB_DFcbvrfBzapPrylnOAjMI")
        return url.toString()
    }
}
