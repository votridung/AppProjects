package com.example.lenghia.basicproject.Helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.lenghia.basicproject.Common.Common
import com.example.lenghia.basicproject.Model.PlaceDetail
import com.example.lenghia.basicproject.R
import com.example.lenghia.basicproject.R.id.*
import com.example.lenghia.basicproject.Remote.IGoogleAPIService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_place.*
import kotlinx.android.synthetic.main.custom_info_window.view.*
import retrofit2.Call
import retrofit2.Response

class CustomInfoWindow(context: Context) : GoogleMap.InfoWindowAdapter {

    var mView: View? = null

    internal lateinit var mService: IGoogleAPIService
    var mPlace: PlaceDetail? = null

    var mContext: Context? = null

    init {
        this.mContext = context
        mView = LayoutInflater.from(context)
                .inflate(R.layout.custom_info_window, null)
        mService = Common.googleAPIService
    }

    fun loadAllInfo(){

        mService = Common.googleAPIService

        mView!!.place_name_txt.text = ""
        mView!!.place_address_txt.text = ""
        mView!!.place_open_hour_txt.text = ""

        mView!!.txt_map.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.result!!.url))
            startActivity(mContext!!,mapIntent,null)
        }

        if (Common.currentResult!!.opening_hours != null) {
            mView!!.place_open_hour_txt.text = " Open now : " + Common.currentResult!!.opening_hours!!.open_now
        } else
            mView!!.place_open_hour_txt.visibility = View.GONE

        if (Common.currentResult!!.photos != null && Common.currentResult!!.photos!!.size > 0)
            Picasso.with(mContext)
                    .load(getPhotoOfPlace(Common.currentResult!!.photos!![0].photo_reference!!, 1000))
                    .into(mView!!.photo_image)

    }

    override fun getInfoWindow(marker: Marker): View {

        //loadAllInfo()

        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.place_id!!))
                .enqueue(object : retrofit2.Callback<PlaceDetail> {
                    override fun onResponse(call: Call<PlaceDetail>?, response: Response<PlaceDetail>?) {
                        mPlace = response!!.body()

                        mView!!.place_address_txt.text = mPlace!!.result!!.formatted_address
                        mView!!.place_name_txt.text = mPlace!!.result!!.name

                    }

                    override fun onFailure(call: Call<PlaceDetail>?, t: Throwable?) {
                        Toast.makeText(mContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
                    }

                })

        return mView!!
    }

    private fun getPlaceDetailUrl(place_id: String?): String {
        var url = StringBuffer("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?placeid=$place_id")
        url.append("&key=AIzaSyD9td1ZlCRuB_DFcbvrfBzapPrylnOAjMI")
        return url.toString()
    }

    private fun getPhotoOfPlace(photo_reference: String, maxWidth: Int): String {
        var url = StringBuffer("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photo_reference=$photo_reference")
        url.append("&key=AIzaSyD9td1ZlCRuB_DFcbvrfBzapPrylnOAjMI")
        return url.toString()
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}
