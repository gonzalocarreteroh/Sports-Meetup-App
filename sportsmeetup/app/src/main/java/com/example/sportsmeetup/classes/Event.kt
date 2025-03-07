package com.example.sportsmeetup.classes

import java.io.Serializable
import java.util.Date

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

data class Event(var docId: String,var sport:String,var time: Long,var venue: String,var creator: String,var participants:List<String>,var creatorName: String) : Serializable{

    var cal :Calendar = Calendar.getInstance().apply{
        timeInMillis = this@Event.time
    }

    init{
        cal.timeInMillis = time
    }
    fun getDate(): String {
        var df :SimpleDateFormat = SimpleDateFormat("dd")

        return df.format(cal.time)
    }

    fun getTimeLong() : Long{
        return time
    }

    fun getTime(): String{
        var df :SimpleDateFormat = SimpleDateFormat("HH:mm")

        return df.format(cal.time)
    }

    fun getMonth(): String {
        var df :SimpleDateFormat = SimpleDateFormat("MMMM")

        return df.format(cal.time)
    }

    fun getMonthandDate(): String{
        return getMonth() + " " +getDate()
    }

    fun getFullDate(): String{
        var df :SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

        return df.format(cal.time)
    }

    override fun toString(): String {
        var rstring = String.format("%-4s %16s %16s",getTime(),sport,creatorName)
        if(participants.size > 1)
            rstring += "(+"+(participants.size - 1)+")"
        return rstring
    }
}