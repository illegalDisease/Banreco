package com.illegaldisease.banreco.databaserelated

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateFormat
import com.illegaldisease.banreco.R
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.doAsyncResult
import java.util.*
import kotlin.collections.ArrayList

class EventHandler(private val context: Context) { //Don't construct this object more than once. PLEASE.
    companion object {
        val futureEvents : MutableList<EventsRemastered> = ArrayList()
        val pastEvents : MutableList<EventsRemastered> = ArrayList()
    }
    init {
        fillOutTables() //Try to reach db immediately.
    }
    private fun REMOVETHIS(){
        //Dummy data entered. Now use this function properly.

    }
    private fun convertToBitmap(value : ByteArray) : Bitmap {
        return BitmapFactory.decodeByteArray(value,0,value.size) ?: BitmapFactory.decodeResource(context.resources,R.drawable.ic_camera)
        //Return placeholder if you could not decode the image.
    }
    private fun convertToDateString(timestamp : Int) : String {
        var calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp.toLong() * 1000
        return DateFormat.format("dd-MM-yyyy HH:MM",calendar).toString()
    }
    fun fillOutTables(){
        //Also inspects them if they belong to past or future. Will consider exact this time as past.
        futureEvents.clear()
        pastEvents.clear() //Just in case, i don't ever want duplicate entries.
        var wholeEvents : MutableList<EventModel> = ArrayList() //Temporary list.
        val dbCall = context.database.use {
            select(EventModel.tableName).exec {

                var parser = rowParser{ id : Int, date : Int, photoTaken : ByteArray ->
                    EventModel(id,date,photoTaken)
                }
                wholeEvents = parseList(parser).toMutableList()
//                if(count <= 0) close() //Take care of nonexistant data.
//                moveToFirst()
//                val thisId = getInt(getColumnIndex(EventModel.idColumn))
//                var thisDate = getInt(getColumnIndex(EventModel.dateColumn))
//                var thisImage = getBlob(getColumnIndex(EventModel.imageColumn))
//                var
//
//                moveToNext()
            }
        }
        dbCall.doAsyncResult {
            //Take care of NullPointerException.
            wholeEvents!!.forEach { current ->
                addEventToLists(current)
            }
        }
    }
    fun deleteEvent(eventToDelete : EventsRemastered){
        //Deletes from database and relevant list as well. Don't forget to check already nonexistent data.
        if(futureEvents.contains(eventToDelete)){
            futureEvents.remove(eventToDelete)
        }
        else{
            //If it is not on future, it is probably belong to the past.
            pastEvents.remove(eventToDelete)
        }
        //Future or past, they belong at the same table.
        context.database.use {
            var rowsDeleted = delete(EventModel.tableName,"${EventModel.idColumn} = ${eventToDelete.id}", arrayOf())
            //Probably faulty syntax for anko. Your app might be broken if they fix that. For a workaround, i provided empty array to satisfy WhereArgs.
        }
    }
    fun addEvent(/* currentModel: EventModel */){
        //TODO: Currently we don't know how to retrieve image and data from activity itself. Fix this when you implement that.
//        var baos = ByteArrayOutputStream()
//        val tmpBitmap = context.resources.getDrawable(R.drawable.photo1,context.resources.newTheme()) as BitmapDrawable
//        val tmpBitmap2 = tmpBitmap.bitmap
//        tmpBitmap2.compress(Bitmap.CompressFormat.PNG,100,baos)
//        val thisTime = System.currentTimeMillis() / 1000
//        val thisImage = baos.toByteArray()
//        var thisContent = ContentValues()
//        thisContent.put(EventModel.imageColumn, thisImage)
//        var totalRowAffected : Long = 0
//        context.database.use {
//            totalRowAffected += insert(EventModel.tableName,null,thisContent)
//        }
//        addEventToLists(currentModel) //Also add to the list. Normally not necessary but who knows what will happen.
    }
    private fun addEventToLists(currentModel : EventModel){
        //This function will separate entries using current date ( past or future ) and place it to Lists.
        val thisDate = convertToDateString(currentModel.date)
        val thisPhoto = convertToBitmap(currentModel.photoTaken)
        //TODO: If you wanna change shown database structure, you will want to check here too.

        if(System.currentTimeMillis()/1000 >= currentModel.date){
            //Why would i compare two calendars, when i have timestamps already ???
            //Here is past events.
            pastEvents.add(EventsRemastered(currentModel.id, thisDate,thisPhoto))
        }
        else{
            futureEvents.add(EventsRemastered(currentModel.id, thisDate,thisPhoto))
        }
    }
}