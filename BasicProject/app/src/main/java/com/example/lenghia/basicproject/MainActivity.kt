package com.example.lenghia.basicproject

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.lenghia.basicproject.Common.Common
import com.example.lenghia.basicproject.Helper.CustomInfoWindow
import com.example.lenghia.basicproject.Model.MyPlaces
import com.example.lenghia.basicproject.Remote.IGoogleAPIService
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private var places: PlaceAutocompleteFragment? = null

    private lateinit var mMap: GoogleMap
    private var latitude: Double? = 0.toDouble()
    private var longitude: Double? = 0.toDouble()

    private lateinit var mlastLocation: Location
    private var mMarker: Marker? = null


    var black = Color.BLACK

    //Location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    lateinit var mService: IGoogleAPIService

    internal lateinit var currentPlace: MyPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

//        actionBar.title = null

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottom_view.itemIconTintList = null
        bottom_view.itemTextColor = ColorStateList.valueOf(black)


        places = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        places!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place?) {
                Toast.makeText(this@MainActivity, " Moving on the place you picked ! ", Toast.LENGTH_SHORT).show()
            }

            override fun onError(status: Status) {

                Toast.makeText(this@MainActivity, "" + status.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        //init service
        mService = Common.googleAPIService
        setInfoInHeader()

        //Request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        } else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        bottom_view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_hospital -> nearByPlace("hospital")
                R.id.action_market -> nearByPlace("market")
                R.id.action_restaurant -> nearByPlace("restaurant")
                R.id.action_school -> nearByPlace("school")
                R.id.action_atm -> nearByPlace("atm")

            }
            true
        }

    }

    private fun setInfoInHeader() {
        var mIntent: Intent = getIntent()

        var mEmail = mIntent.getStringExtra("email")
        var mName = mIntent.getStringExtra("name")
        Log.d("mEmail", mEmail.toString());

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val hView = navigationView.getHeaderView(0)
        val txt_user_name = hView.findViewById(R.id.txtName) as TextView
        val txt_email = hView.findViewById(R.id.txtEmail) as TextView

        txt_user_name.text = "Current Name : " + mName.toString()
        txt_email.text = "Current Email :" + mEmail.toString()

    }

    private fun nearByPlace(typePlace: String) {
        mMap.clear()
        val url = getUrl(latitude, longitude, typePlace)

        mService.getNearbyPlaces(url)
                .enqueue(object : Callback<MyPlaces> {
                    override fun onFailure(call: Call<MyPlaces>?, t: Throwable?) {
                        Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<MyPlaces>?, response: Response<MyPlaces>?) {
                        currentPlace = response!!.body()!!
                        if (response.isSuccessful) {
                            for (i in 0 until response.body()!!.results!!.size) {
                                val markerOptions = MarkerOptions()
                                val googlePlace = response.body()!!.results!![i]
                                val lat = googlePlace.geometry!!.location!!.lat
                                val lng = googlePlace.geometry!!.location!!.lng
                                val placeName = googlePlace.name
                                val latLng = LatLng(lat, lng)

                                markerOptions.position(latLng)
                                markerOptions.title(placeName)
                                if (typePlace.equals("hospital"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
                                else if (typePlace.equals("market"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))
                                else if (typePlace.equals("restaurant"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                                else if (typePlace.equals("school"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                                else if (typePlace.equals("atm"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_atm))
                                else
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                                markerOptions.snippet(i.toString()) //assign index for market

                                mMap.addMarker(markerOptions)
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude!!, longitude!!)))
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
                            }
                        }
                    }
                })
    }

    private fun getUrl(latitude: Double?, longitude: Double?, typePlace: String): String {

        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=10000")
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyD9td1ZlCRuB_DFcbvrfBzapPrylnOAjMI")

        Log.d("LTN_DEBUG", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()

    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                mlastLocation = p0!!.locations.get(p0.locations.size - 1) // get last location

                if (mMarker != null) {
                    mMarker!!.remove()
                }

                latitude = mlastLocation.latitude
                longitude = mlastLocation.longitude

                val latLng = LatLng(latitude!!, longitude!!)
                val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title("Your position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                mMarker = mMap.addMarker(markerOptions)

                //move camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))

            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            return false
        } else
            return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                            mMap.isMyLocationEnabled = true
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission Denied ! ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {
                startActivity(Intent(this@MainActivity, SenderActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        } else
            mMap.isMyLocationEnabled = true
        //enable zoom control
        mMap.uiSettings.isZoomControlsEnabled = true
        //mMap.setInfoWindowAdapter(CustomInfoWindow(this))

        mMap.setOnMarkerClickListener { marker ->
            Common.currentResult = currentPlace.results!![Integer.parseInt(marker.snippet)]

            startActivity(Intent(this@MainActivity, ViewPlace::class.java))
            true
        }

    }

}
