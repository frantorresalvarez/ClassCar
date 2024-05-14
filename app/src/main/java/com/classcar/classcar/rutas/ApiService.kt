package com.classcar.classcar.rutas

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/v2/directions/driving-car") //Referencia a la ruta
    suspend fun getRoute(  //Suspend para generar tiempo de espera
        @Query("api_key") key: String,
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ): Response<RouteResponse>

}