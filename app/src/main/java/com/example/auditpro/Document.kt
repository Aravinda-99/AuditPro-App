package com.example.auditpro

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.AbsListView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Document : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
        private const val TAG = "Documents"
    }

    private lateinit var storageFunctions: StorageFunctions
    private var items = ArrayList<DocItem>()
    private lateinit var docItemAdapter: DocItemAdapter
    private lateinit var itemsListView: ListView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.actionbar)
        }

        storageFunctions = StorageFunctions(this)
        fab = findViewById(R.id.fab)
        itemsListView = findViewById(R.id.itemsList)

        val empty: TextView = findViewById(R.id.emptyTextView)
        empty.text = getString(R.string.listEmptyText)
        val emptyView: FrameLayout = findViewById(R.id.emptyView)
        itemsListView.emptyView = emptyView

        populateListView()
        onFabClick()
        hideFab()
    }

    private fun insertData(name: String, description: String, section: String, company: String, requestDate: LocalDate?, requestTime: LocalTime?) {
        val insertData = storageFunctions.insertData(name, description, section, company, requestDate, requestTime)
        if (insertData) {
            populateListView()
            toastMsg("Added successfully!")
            createNotification("Data successfully added")
            Log.d(TAG, "insertDataToDb: Inserted data into database")
        } else {
            toastMsg("Something went wrong")
        }
    }

    private fun populateListView() {
        items = storageFunctions.getAllData()
        docItemAdapter = DocItemAdapter(this, items)
        itemsListView.adapter = docItemAdapter
        docItemAdapter.notifyDataSetChanged()
        Log.d(TAG, "populateListView: Displaying data in list view")
    }

    private fun hideFab() {
        itemsListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    fab.show()
                } else {
                    fab.hide()
                }
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })
    }

    private fun onFabClick() {
        fab.setOnClickListener {
            showAddDialog()
            Log.d(TAG, "onFabClick: Opened edit dialog")
        }
    }

    private fun showAddDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val editName: EditText = dialogView.findViewById(R.id.edit_name)
        val editDescription: EditText = dialogView.findViewById(R.id.edit_description)
        val editSection: EditText = dialogView.findViewById(R.id.edit_section)
        val editCompany: EditText = dialogView.findViewById(R.id.edit_company)
        val dateText: TextView = dialogView.findViewById(R.id.date)
        val timeText: TextView = dialogView.findViewById(R.id.time)

        val date = LocalDate.now()
        val time = LocalTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        dateText.text = date.format(dateFormatter)
        timeText.text = time.format(DateTimeFormatter.ofPattern("hh:mm a"))

        dateText.setOnClickListener {
            val cal = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val newDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    dateText.text = newDate.format(dateFormatter)
                    Log.d(TAG, "onDateSet: Date has been set successfully")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        }

        timeText.setOnClickListener {
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                timeText.text = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            }, time.hour, time.minute, false)
            timePickerDialog.show()
        }

        dialogBuilder.setTitle("Let's add new task!")
        dialogBuilder.setPositiveButton("Done") { _, _ ->
            val name = editName.text.toString()
            val description = editDescription.text.toString()
            val section = editSection.text.toString()
            val company = editCompany.text.toString()
            val requestDate = LocalDate.parse(dateText.text.toString(), DateTimeFormatter.ofPattern("d MMMM yyyy"))
            val requestTime = LocalTime.parse(timeText.text.toString(), DateTimeFormatter.ofPattern("hh:mm a"))

            if (name.isNotEmpty() && description.isNotEmpty() && section.isNotEmpty() && company.isNotEmpty()) {
                insertData(name, description, section, company, requestDate, requestTime)
            } else {
                toastMsg("Oops, Cannot set an incomplete ToDo!!!")
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun toastMsg(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Document Update")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}
