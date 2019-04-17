package com.example.taskapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import io.realm.Realm

class TaskAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // SDKバージョンが26以上の場合、チャネルを設定する必要がある
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Channel description"
            notificationManager.createNotificationChannel(channel)
        }

        // 通知の設定を行う
        val notificationBuilder = NotificationCompat.Builder(context, "default")
        notificationBuilder.setSmallIcon(R.drawable.small_icon)
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.large_icon))
        notificationBuilder.setWhen(System.currentTimeMillis())
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL)
        notificationBuilder.setAutoCancel(true)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val taskId = intent!!.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        val task = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        // タスクの情報を設定する
        notificationBuilder.setTicker(task!!.title)   // 5.0以降は表示されない
        notificationBuilder.setContentTitle(task.title)
        notificationBuilder.setContentText(task.contents)

        // 通知をタップしたらアプリを起動するようにする
        val startAppIntent = Intent(context, MainActivity::class.java)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0)
        notificationBuilder.setContentIntent(pendingIntent)

        // 通知を表示する
        notificationManager.notify(task.id, notificationBuilder.build())
        realm.close()
    }
}
