/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.iosched.shared.data.config

import android.content.res.Resources.NotFoundException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.samples.apps.iosched.shared.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * [AppConfigDataSource] implementation backed by Remote Config.
 */
open class RemoteAppConfigDataSource @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    configSettings: FirebaseRemoteConfigSettings,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AppConfigDataSource {

    private val _attributesLiveDataMap: Map<String, MutableLiveData<String>> = mapOf(
        Pair(REGISTRATION_DAY1_START_TIME, MutableLiveData()),
        Pair(REGISTRATION_DAY1_END_TIME, MutableLiveData()),
        Pair(REGISTRATION_DAY2_START_TIME, MutableLiveData()),
        Pair(REGISTRATION_DAY2_END_TIME, MutableLiveData()),
        Pair(BREAKFAST_DAY1_START_TIME, MutableLiveData()),
        Pair(BREAKFAST_DAY1_END_TIME, MutableLiveData()),
        Pair(BREAKFAST_DAY2_START_TIME, MutableLiveData()),
        Pair(BREAKFAST_DAY2_END_TIME, MutableLiveData()),
        Pair(KEYNOTE_START_TIME, MutableLiveData()),
        Pair(KEYNOTE_END_TIME, MutableLiveData()),
        Pair(LUNCH_DAY1_START_TIME, MutableLiveData()),
        Pair(LUNCH_DAY1_END_TIME, MutableLiveData()),
        Pair(LUNCH_DAY2_START_TIME, MutableLiveData()),
        Pair(LUNCH_DAY2_END_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY1_1_START_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY1_1_END_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY1_2_START_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY1_2_END_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY1_3_START_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY1_3_END_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY2_1_START_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY2_1_END_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY2_2_START_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY2_2_END_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY2_3_START_TIME, MutableLiveData()),
        Pair(SESSIONS_DAY2_3_END_TIME, MutableLiveData()),
        Pair(TEA_BREAK_DAY1_START_TIME, MutableLiveData()),
        Pair(TEA_BREAK_DAY1_END_TIME, MutableLiveData()),
        Pair(TEA_BREAK_DAY2_START_TIME, MutableLiveData()),
        Pair(TEA_BREAK_DAY2_END_TIME, MutableLiveData()),
        Pair(PARTY_START_TIME, MutableLiveData()),
        Pair(PARTY_END_TIME, MutableLiveData()),
        Pair(LABEL_REGISTRATION, MutableLiveData()),
        Pair(LABEL_KEYNOTE, MutableLiveData()),
        Pair(LABEL_SESSIONS, MutableLiveData()),
        Pair(LABEL_BREAKFAST, MutableLiveData()),
        Pair(LABEL_LUNCH, MutableLiveData()),
        Pair(LABEL_TEA_BREAK, MutableLiveData()),
        Pair(LABEL_PARTY, MutableLiveData())
    )

    private val cacheExpirySeconds: Long

    init {
        // updating the remote config with the last fetched values
        firebaseRemoteConfig.activateFetched()
        // Set cache expiration to 0s when debugging to allow easy testing, otherwise
        // use the default value
        cacheExpirySeconds = if (configSettings.isDeveloperModeEnabled) {
            0
        } else {
            DEFAULT_CACHE_EXPIRY_S
        }
        firebaseRemoteConfig.activateFetched() // update active config with the last fetched values
        updateStrings()
        firebaseRemoteConfig.fetch(cacheExpirySeconds).addOnCompleteListener { task ->
            // Async
            if (task.isSuccessful) {
                firebaseRemoteConfig.activateFetched()
                updateStrings()
            }
        }
    }

    override fun getString(key: String): LiveData<String> =
        _attributesLiveDataMap[key] ?: throw NotFoundException("Value for $key not found")

    private fun updateStrings() {
        _attributesLiveDataMap.map { (key, liveData) ->
            liveData.value = firebaseRemoteConfig.getString(key)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun syncStrings() {
        withContext(ioDispatcher) {
            val task = firebaseRemoteConfig.fetch(cacheExpirySeconds)
            suspendCancellableCoroutine<Unit> { continuation ->
                task.addOnCompleteListener {
                    firebaseRemoteConfig.activateFetched()
                    updateStrings()
                    if (!continuation.isActive) {
                        return@addOnCompleteListener
                    }
                    continuation.resume(Unit) {}
                }
            }
        }
    }

    companion object {
        const val REGISTRATION_DAY1_START_TIME = "registration_day1_start_time"
        const val REGISTRATION_DAY1_END_TIME = "registration_day1_end_time"
        const val REGISTRATION_DAY2_START_TIME = "registration_day2_start_time"
        const val REGISTRATION_DAY2_END_TIME = "registration_day2_end_time"
        const val BREAKFAST_DAY1_START_TIME = "breakfast_day1_start_time"
        const val BREAKFAST_DAY1_END_TIME = "breakfast_day1_end_time"
        const val BREAKFAST_DAY2_START_TIME = "breakfast_day2_start_time"
        const val BREAKFAST_DAY2_END_TIME = "breakfast_day2_end_time"
        const val KEYNOTE_START_TIME = "keynote_start_time"
        const val KEYNOTE_END_TIME = "keynote_end_time"
        const val LUNCH_DAY1_START_TIME = "lunch_day1_start_time"
        const val LUNCH_DAY1_END_TIME = "lunch_day1_end_time"
        const val LUNCH_DAY2_START_TIME = "lunch_day2_start_time"
        const val LUNCH_DAY2_END_TIME = "lunch_day2_end_time"
        const val SESSIONS_DAY1_1_START_TIME = "sessions_day1_1_start_time"
        const val SESSIONS_DAY1_1_END_TIME = "sessions_day1_1_end_time"
        const val SESSIONS_DAY1_2_START_TIME = "sessions_day1_2_start_time"
        const val SESSIONS_DAY1_2_END_TIME = "sessions_day1_2_end_time"
        const val SESSIONS_DAY1_3_START_TIME = "sessions_day1_3_start_time"
        const val SESSIONS_DAY1_3_END_TIME = "sessions_day1_3_end_time"
        const val SESSIONS_DAY2_1_START_TIME = "sessions_day2_1_start_time"
        const val SESSIONS_DAY2_1_END_TIME = "sessions_day2_1_end_time"
        const val SESSIONS_DAY2_2_START_TIME = "sessions_day2_2_start_time"
        const val SESSIONS_DAY2_2_END_TIME = "sessions_day2_2_end_time"
        const val SESSIONS_DAY2_3_START_TIME = "sessions_day2_3_start_time"
        const val SESSIONS_DAY2_3_END_TIME = "sessions_day2_3_end_time"
        const val TEA_BREAK_DAY1_START_TIME = "tea_break_day1_start_time"
        const val TEA_BREAK_DAY1_END_TIME = "tea_break_day1_end_time"
        const val TEA_BREAK_DAY2_START_TIME = "tea_break_day2_start_time"
        const val TEA_BREAK_DAY2_END_TIME = "tea_break_day2_end_time"
        const val PARTY_START_TIME = "party_start_time"
        const val PARTY_END_TIME = "party_end_time"
        const val LABEL_REGISTRATION = "label_registration"
        const val LABEL_KEYNOTE = "label_keynote"
        const val LABEL_SESSIONS = "label_sessions"
        const val LABEL_BREAKFAST = "label_breakfast"
        const val LABEL_LUNCH = "label_lunch"
        const val LABEL_TEA_BREAK = "label_tea_break"
        const val LABEL_PARTY = "label_party"

        val DEFAULT_CACHE_EXPIRY_S = TimeUnit.MINUTES.toSeconds(12)
    }
}
