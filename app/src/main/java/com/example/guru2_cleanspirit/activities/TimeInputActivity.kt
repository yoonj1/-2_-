package com.example.guru2_cleanspirit

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.app.TimePickerDialog
import android.widget.TimePicker
import java.util.Calendar

class TimePickerFragment(private val onTimeSet: (Int, Int) -> Unit) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        onTimeSet(hourOfDay, minute)
    }
}
