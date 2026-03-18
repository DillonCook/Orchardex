package com.dillon.orcharddex.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dillon.orcharddex.MainActivity
import com.dillon.orcharddex.R
import com.dillon.orcharddex.data.local.ReminderEntity

object ReminderNotificationManager {
    const val CHANNEL_ID = "orcharddex_reminders"

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Orchard reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Local reminders for orchard tasks"
        }
        manager.createNotificationChannel(channel)
    }

    fun notify(context: Context, reminder: ReminderEntity, treeLabel: String?) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val body = buildString {
            append(treeLabel ?: "Orchard task")
            if (reminder.notes.isNotBlank()) {
                append("\n")
                append(reminder.notes)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(reminder.title)
            .setContentText(treeLabel ?: reminder.notes.ifBlank { "Open OrchardDex for details." })
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)
    }
}
