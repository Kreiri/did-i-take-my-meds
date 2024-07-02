package dev.corruptedark.diditakemymeds.util

import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.MaterialToolbar
import dev.corruptedark.diditakemymeds.R
import dev.corruptedark.diditakemymeds.data.models.DoseRecord
import dev.corruptedark.diditakemymeds.data.models.DoseUnit
import dev.corruptedark.diditakemymeds.data.models.Medication
import dev.corruptedark.diditakemymeds.data.models.MedicationType
import dev.corruptedark.diditakemymeds.data.models.RepeatSchedule
import java.util.Calendar
import java.util.concurrent.TimeUnit

@BindingAdapter("zeroContentInsetStart")
fun setNoContentInsetStart(view: MaterialToolbar, noContentInsetStart: Boolean) {
    if (noContentInsetStart) {
        view.setContentInsetsRelative(0, view.contentInsetEnd)
    }
}

@BindingAdapter("zeroNavigationContentInsetStart")
fun setNoNavigationContentInsetStart(
    view: MaterialToolbar,
    zeroNavigationContentInsetStart: Boolean
) {
    if (zeroNavigationContentInsetStart) {
        view.contentInsetStartWithNavigation = 0
    }
}

@BindingAdapter("medicationType")
fun setMedicationType(view: TextView, medicationType: MedicationType) {
    val text = view.context.getString(R.string.type_label_format, medicationType.name)
    view.text = text
}

@BindingAdapter("rxNumber")
fun setRxNumber(view: TextView, medication: Medication) {
    val context = view.context
    val rxNumber = if (medication.rxNumber == Medication.UNDEFINED) {
        context.getString(R.string.undefined)
    } else medication.rxNumber

    val text = view.context.getString(R.string.rx_number_label_format, rxNumber)
    view.text = text
}

@BindingAdapter("doseUnit", "amountPerDose")
fun setDoseInfo(view: TextView, doseUnit: DoseUnit, amountPerDose: Double) {
    val doseValueString = amountPerDose.toBigDecimal().stripTrailingZeros().toPlainString()
    val doseUnitString = doseUnit.unit
    val text =
        view.context.getString(R.string.dose_amount_label_format, doseValueString, doseUnitString)
    view.text = text
}

@BindingAdapter("remainingDoses")
fun setRemainingDoses(view: TextView, remainingDoses: Int) {
    val text = view.context.getString(R.string.remaining_doses_label_format, remainingDoses)
    view.text = text
}

@BindingAdapter("nextDose")
fun setNextDose(view: TextView, medication: Medication) {
    val context = view.context
    val doseString = medication.nextDoseString(context)
    val text = context.getString(R.string.next_dose_label, doseString)
    view.text = text
}

@BindingAdapter("closestDose")
fun setClosestDose(view: TextView, medication: Medication) {
    val context = view.context
    val doseString = medication.closestDoseString(context)
    val text = context.getString(R.string.next_dose_label, doseString)
    view.text = text
}

@BindingAdapter("sinceLastDose")
fun setSinceLastDose(view: TextView, medication: Medication) {
    val text = medication.timeSinceLastDoseString(view.context)
    view.text = text

}

@BindingAdapter("pharmacy")
fun setPharmacy(view: TextView, medication: Medication) {
    val context = view.context
    val pharmacy = if (medication.pharmacy == Medication.UNDEFINED) {
        context.getString(R.string.undefined)
    } else medication.pharmacy

    val text = view.context.getString(R.string.pharmacy_label_format, pharmacy)
    view.text = text
}

@BindingAdapter("justTookIt")
fun setJustTookIt(view: TextView, medication: Medication) {
    val text = if (medication.closestDoseAlreadyTaken() && !medication.isAsNeeded()) {
        view.context.getString(R.string.took_this_already)
    } else {
        view.context.getString(R.string.i_just_took_it)
    }
    view.text = text
}


@BindingAdapter("timeTaken")
fun setTimeTaken(view: TextView, doseRecord: DoseRecord) {
    val context = view.context
    val doseString = medicationDoseString(context, doseRecord.doseTime)
    val text = context.getString(R.string.time_taken, doseString)
    view.text = text
}

@BindingAdapter("closestDose")
fun setClosestDose(view: TextView, doseRecord: DoseRecord) {
    val context = view.context
    val doseString = medicationDoseString(context, doseRecord.closestDose)
    val text = context.getString(R.string.closest_dose_label, doseString)
    view.text = text
}


@BindingAdapter("nextDose")
fun setNextDose(view: TextView, doseRecord: DoseRecord) {
    val context = view.context
    val doseString = medicationDoseString(context, doseRecord.closestDose)
    val text = context.getString(R.string.next_dose_label, doseString)
    view.text = text
}

@BindingAdapter("timeFromMillis")
fun setTimeFromMillis(view: TextView, millis: Long) {
    val context = view.context
    val text = context.formatTime(millis)
    view.text = text
}
@BindingAdapter("dateFromMillis")
fun setDateFromMillis(view: TextView, millis: Long) {
    val context = view.context
    val dateFormat = context.getString(R.string.date_format)
    val text = DateFormat.format(dateFormat, millis)
    view.text = text
}

@BindingAdapter("schedule", "blankScheduleText")
fun setSchedule(view: TextView, schedule: RepeatSchedule?, blankScheduleText: String) {
    val context = view.context
    val text = if (schedule != null && schedule != RepeatSchedule.BLANK) {
        val calendar = Calendar.getInstance().apply { schedule.fillCalendar(this) }
        val formattedTime = context.formatTime(calendar)
        val formattedDate = context.formatDate(calendar)
        val activeDays = schedule.daysBetween
        context.getString(R.string.schedule_format, formattedTime, formattedDate, activeDays, 0, 0, 0)
    } else {
        blankScheduleText
    }
    view.text = text
}

@BindingAdapter("medicationTakenTime")
fun setTakenTime(view: TextView, medication: Medication) {
    val context = view.context
    val text = if (medication.active) {
        if (medication.isAsNeeded()) {
            context.getString(R.string.taken_as_needed)
        } else {
            val closestDoseMillis = medication .calculateClosestDose().timeInMillis
            context.formatTime(closestDoseMillis)
        }
    } else {
        context.getString(R.string.inactive)
    }
    view.text = text
}

@BindingAdapter("medicationTakenStatus")
fun setTakenStatus(view: TextView, medication: Medication) {
    val context = view.context
    if (medication.active && !medication.isAsNeeded()) {
        val text = if (medication.closestDoseAlreadyTaken()) {
            context.getString(R.string.taken)
        } else {
            context.getString(R.string.not_taken)
        }
        view.text = text
        view.visibility = View.VISIBLE
    } else {
        view.text = ""
        view.visibility = View.GONE
    }
}

@BindingAdapter("medicationActive")
fun setMedicationActive(view: View, medication: Medication) {
    if (medication.active) {
        view.alpha = 1.0f
    } else {
        view.alpha = 0.7f;
    }
}