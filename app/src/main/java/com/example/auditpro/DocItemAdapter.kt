package com.example.auditpro

import android.app.TimePickerDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DocItemAdapter(private val context: Context, private val arrayList: ArrayList<DocItem>) : BaseAdapter() {
    private val handler = Handler(Looper.getMainLooper())

    override fun getCount(): Int = arrayList.size

    override fun getItem(position: Int): Any = arrayList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: layoutInflater.inflate(R.layout.row_item, parent, false)

        val nameTextView: TextView = view.findViewById(R.id.title)
        val descriptionTextView: TextView = view.findViewById(R.id.description)
        val companyTextView: TextView = view.findViewById(R.id.company)
        val sectionTextView: TextView = view.findViewById(R.id.section)
        val dateTextView: TextView = view.findViewById(R.id.date)
        val timeTextView: TextView = view.findViewById(R.id.time)
        val remainingTimeTextView: TextView = view.findViewById(R.id.remaining_time)
        val delImageView: ImageView = view.findViewById(R.id.delete)
        val updateImageView: ImageView = view.findViewById(R.id.update)

        delImageView.setOnClickListener { deleteItem(position) }
        updateImageView.setOnClickListener { showUpdateDialog(position) }

        val docItem = arrayList[position]
        nameTextView.text = docItem.name
        descriptionTextView.text = docItem.description
        companyTextView.text = docItem.company
        sectionTextView.text = docItem.section
        dateTextView.text = docItem.requestDate.toString()
        timeTextView.text = docItem.requestTime?.toString() ?: "No time set"

        // Start updating the remaining time every second
        startUpdatingRemainingTime(docItem, remainingTimeTextView)

        return view
    }

    private fun startUpdatingRemainingTime(docItem: DocItem, remainingTimeTextView: TextView) {
        handler.post(object : Runnable {
            override fun run() {
                val remainingTime = calculateRemainingTime(docItem.requestDate, docItem.requestTime, docItem.name)
                remainingTimeTextView.text = remainingTime
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun calculateRemainingTime(requestDate: LocalDate?, requestTime: LocalTime?, taskName: String): String {
        return if (requestDate != null && requestTime != null) {
            val now = LocalDateTime.now()
            val requestDateTime = LocalDateTime.of(requestDate, requestTime)
            val duration = Duration.between(now, requestDateTime)

            if (duration.isNegative) {
                // Time has passed, send a notification
                val notificationId = System.currentTimeMillis().toInt() // Unique notification ID
                NotificationUtils.sendNotification(context, notificationId, "Task Overdue", "The task '$taskName' is overdue.")
                "Time Passed"
            } else {
                val hours = duration.toHours()
                val minutes = (duration.toMinutes() % 60)
                val seconds = (duration.seconds % 60)
                "${hours}h ${minutes}m ${seconds}s"
            }
        } else {
            "Date or time not set"
        }
    }

    private fun showUpdateDialog(position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.update_dialog, null)
        dialogBuilder.setView(dialogView)

        val editName: EditText = dialogView.findViewById(R.id.edit_name)
        val descriptionText: EditText = dialogView.findViewById(R.id.edit_description)
        val sectionText: EditText = dialogView.findViewById(R.id.edit_section)
        val companyText: EditText = dialogView.findViewById(R.id.edit_company)
        val dateText: TextView = dialogView.findViewById(R.id.date)
        val timeText: TextView = dialogView.findViewById(R.id.time)

        val docItem = arrayList[position]
        editName.setText(docItem.name)
        editName.isEnabled = false
        descriptionText.setText(docItem.description)
        sectionText.setText(docItem.section)
        companyText.setText(docItem.company)
        dateText.text = docItem.requestDate?.toString() ?: ""
        timeText.text = docItem.requestTime?.toString() ?: "Select Time"

        // Set onClickListener to select time
        timeText.setOnClickListener {
            val currentTime = docItem.requestTime ?: LocalTime.now()
            val timePicker = TimePickerDialog(context, { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                timeText.text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }, currentTime.hour, currentTime.minute, true)
            timePicker.show()
        }

        dialogBuilder.setPositiveButton("Update") { dialog, _ ->
            val updatedDescription = descriptionText.text.toString()
            val updatedSection = sectionText.text.toString()
            val updatedCompany = companyText.text.toString()
            val updatedRequestDate = LocalDate.parse(dateText.text.toString())
            val updatedRequestTime = if (timeText.text.toString() != "Select Time") {
                LocalTime.parse(timeText.text.toString())
            } else {
                null // Handle case where no time is selected
            }

            updateItemFromDb(docItem.name, updatedDescription, updatedSection, updatedCompany, updatedRequestDate, updatedRequestTime)
            notifyDataSetChanged()

            docItem.description = updatedDescription
            docItem.section = updatedSection
            docItem.company = updatedCompany
            docItem.requestDate = updatedRequestDate
            docItem.requestTime = updatedRequestTime
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun updateItemFromDb(name: String, description: String, section: String, company: String, requestDate: LocalDate?, requestTime: LocalTime?) {
        val storageFunctions = StorageFunctions(context)
        try {
            storageFunctions.updateData(name, description, section, company, requestDate, requestTime)
            toastMsg("Updated Successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            toastMsg("Something went wrong")
        }
    }

    private fun deleteItem(position: Int) {
        val docItem = arrayList[position]
        deleteItemFromDb(docItem.name, docItem.description, docItem.section, docItem.company, docItem.requestDate, docItem.requestTime)
        arrayList.removeAt(position)
        notifyDataSetChanged()
    }

    private fun deleteItemFromDb(name: String, description: String, section: String, company: String, requestDate: LocalDate?, requestTime: LocalTime?) {
        val storageFunctions = StorageFunctions(context)
        try {
            storageFunctions.deleteData(name, description, section, company, requestDate)
            toastMsg("Deleted Successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            toastMsg("Something went wrong")
        }
    }

    private fun toastMsg(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
