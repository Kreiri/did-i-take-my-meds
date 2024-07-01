/*
 * Did I Take My Meds? is a FOSS app to keep track of medications
 * Did I Take My Meds? is designed to help prevent a user from skipping doses and/or overdosing
 *     Copyright (C) 2021  Noah Stanford <noahstandingford@gmail.com>
 *
 *     Did I Take My Meds? is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Did I Take My Meds? is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.corruptedark.diditakemymeds.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dev.corruptedark.diditakemymeds.R
import dev.corruptedark.diditakemymeds.data.models.Medication

object AlarmIntentManager {

    fun scheduleNotification(
        context: Context, medication: Medication,
        customTimeMillis: Long? = null,
    ): PendingIntent {
        val alarmIntent = buildNotificationAlarm(context, medication)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        setExact(
            alarmManager, alarmIntent,
            customTimeMillis ?: medication.calculateNextDose().timeInMillis
        )

        val receiver = ComponentName(context, ActionReceiver::class.java)

        context.packageManager.setComponentEnabledSetting(
            receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
        return alarmIntent
    }

    fun buildNotificationAlarm(context: Context, medication: Medication): PendingIntent {
        return Intent(context, ActionReceiver::class.java).let { innerIntent ->
            innerIntent.action = ActionReceiver.NOTIFY_ACTION
            innerIntent.putExtra(context.getString(R.string.med_id_key), medication.id)
            context.broadcastIntentFromIntent(medication.id.toInt(), innerIntent)
        }
    }

    fun setExact(alarmManager: AlarmManager, alarmIntent: PendingIntent, timeInMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (canScheduleExactAlarms(alarmManager)) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent
                    )
                }
            } catch (e: SecurityException) {
                //
            }
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent
            )
        }
    }

    private fun canScheduleExactAlarms(alarmManager: AlarmManager) : Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}