package com.casecode.signature.network.service

import com.casecode.signature.model.SignatureResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("signatures/receiving_signature.php")
    fun receivingSignature(
        @Field("ORDER_NUMBER") orderNumber: String?,
        @Field("RECEIVING_SIGNATURE") receivingSignature: String?
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("signatures/storekeeper_signature.php")
    fun storekeeperSignature(
        @Field("ORDER_NUMBER") orderNumber: String?,
        @Field("STOREKEEPER_SIGNATURE") receivingSignature: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("signatures/get_signature.php")
    fun getSignature(
        @Field("ORDER_NUMBER") orderNumber: String?
    ): Call<SignatureResponse>
}