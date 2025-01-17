/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.amazonaws.services.chime.sdk.meetings.internal.ingestion

import com.amazonaws.services.chime.sdk.meetings.analytics.EventAttributes
import com.amazonaws.services.chime.sdk.meetings.utils.logger.Logger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken

/**
 * EventTypeConverters facilitate the conversion on some common event types
 */
class EventTypeConverters(private val logger: Logger) {
    private val TAG = "EventTypeConverters"
    private val gson: Gson

    init {
        val deserializer: JsonDeserializer<SDKEvent> =
            JsonDeserializer { json, _, _ ->
                val jsonObject = json.asJsonObject

                var name = ""
                var eventAttributes: EventAttributes? = null

                try {
                    name = jsonObject["name"].asString
                } catch (exception: Exception) {
                    logger.error(TAG, "Unable to deserialize name $exception")
                }

                try {
                    val eventAttributeType = object : TypeToken<EventAttributes>() {}.type
                    eventAttributes = Gson().fromJson(jsonObject["eventAttributes"], eventAttributeType)
                } catch (exception: Exception) {
                    logger.error(TAG, "Unable to deserialize eventAttributes $exception")
                }
                SDKEvent(name, eventAttributes ?: mutableMapOf())
            }

        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(SDKEvent::class.java, deserializer)
        gson = gsonBuilder.create()
    }

    fun toMeetingEvent(data: String): SDKEvent = gson.fromJson(data, SDKEvent::class.java)
    fun fromMeetingEvent(event: SDKEvent): String = gson.toJson(event)
}
