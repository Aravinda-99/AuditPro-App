package com.example.auditpro

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.LocalTime

class StorageFunctions(private val context: Context) {

    companion object {
        private const val TAG = "FileHelper"
        private const val FILE_NAME = "todo_list.txt"
    }

    fun insertData(name: String, description: String, section: String, company: String, requestDate: LocalDate?, requestTime: LocalTime?): Boolean {
        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_APPEND).use { fos ->
                BufferedWriter(OutputStreamWriter(fos)).use { writer ->
                    writer.write("$name,$description,$section,$company,${requestDate?.toString() ?: ""},${requestTime?.toString() ?: ""}")
                    writer.newLine()
                    Log.d(TAG, "insertData: Inserting $name to $FILE_NAME")
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateData(name: String, newDescription: String, newSection: String, newCompany: String, newRequestDate: LocalDate?, newRequestTime: LocalTime?): Boolean {
        val docItems = getAllData()
        var isUpdated = false

        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                BufferedWriter(OutputStreamWriter(fos)).use { writer ->
                    for (docItem in docItems) {
                        if (docItem.name == name) {
                            writer.write("$name,$newDescription,$newSection,$newCompany,${newRequestDate?.toString() ?: ""},${newRequestTime?.toString() ?: ""}")
                            isUpdated = true
                        } else {
                            writer.write("${docItem.name},${docItem.description},${docItem.section},${docItem.company},${docItem.requestDate?.toString() ?: ""},${docItem.requestTime?.toString() ?: ""}")
                        }
                        writer.newLine()
                    }
                    Log.d(TAG, "updateData: Updated item with name '$name' in $FILE_NAME")
                }
            }
            isUpdated
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteData(name: String, description: String, section: String, company: String, requestDate: LocalDate?) {
        val docItems = getAllData()
        try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                BufferedWriter(OutputStreamWriter(fos)).use { writer ->
                    for (docItem in docItems) {
                        if (!(docItem.name == name && docItem.description == description && docItem.section == section)) {
                            writer.write("${docItem.name},${docItem.description},${docItem.section},${docItem.company},${docItem.requestDate?.toString() ?: ""},${docItem.requestTime?.toString() ?: ""}")
                            writer.newLine()
                        }
                    }
                    Log.d(TAG, "deleteData: Deleted item with name '$name' from $FILE_NAME")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllData(): ArrayList<DocItem> {
        val docItems = ArrayList<DocItem>()
        try {
            context.openFileInput(FILE_NAME).use { fis ->
                BufferedReader(InputStreamReader(fis)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val parts = line.split(",")
                        if (parts.size == 6) {
                            val name = parts[0]
                            val description = parts[1]
                            val section = parts[2]
                            val company = parts[3]
                            val requestDate = if (parts[4].isNotEmpty()) LocalDate.parse(parts[4]) else null
                            val requestTime = if (parts[5].isNotEmpty()) LocalTime.parse(parts[5]) else null
                            docItems.add(DocItem(name, description, section, company, requestDate, requestTime))
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return docItems
    }
}
