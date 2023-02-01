package com.wellysis.spatchcardio.ex.sdkexample.repository

import com.wellysis.spatchcardio.ex.sdkexample.api.ApiHelper
import javax.inject.Inject

class WellysisRepository @Inject constructor(
    private val apiHelper: ApiHelper
){
    suspend fun test() = apiHelper.test()
}