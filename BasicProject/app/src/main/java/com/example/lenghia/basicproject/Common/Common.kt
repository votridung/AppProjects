package com.example.lenghia.basicproject.Common

import com.example.lenghia.basicproject.Model.Results
import com.example.lenghia.basicproject.Remote.IGoogleAPIService
import com.example.lenghia.basicproject.Remote.RetrofitClient

  object Common {

    private val GOOGLE_API_URL = "https://maps.googleapis.com/"

    var currentResult : Results? = null

    val googleAPIService: IGoogleAPIService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}