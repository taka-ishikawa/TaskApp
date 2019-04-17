package com.example.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*

const val EXTRA_TASK = "com.example.taskapp.TASK"

class MainActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm
    private val mRealmListener = RealmChangeListener<Realm> { reloadListView() }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        fab.setOnClickListener {
            intent = Intent(this@MainActivity, EditActivity::class.java)
            startActivity(intent)
        }

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //Intent MainActivity -> EditActivity
        listViewTasks.setOnItemClickListener { parent, _, position, _ ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, EditActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        //delete the Item
        listViewTasks.setOnItemLongClickListener { parent, _, position, _ ->
            val task = parent.adapter.getItem(position) as Task

            //ダイアログを表示する
            val builderAlertDialog = AlertDialog.Builder(this@MainActivity).apply {
                setTitle("削除")
                setMessage(task.title + "を削除しますか？")

                setPositiveButton("OK") { _: DialogInterface, _: Int ->

                    val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                    mRealm.beginTransaction()
                    results.deleteAllFromRealm()
                    mRealm.commitTransaction()

                    val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                    val resultPendingIntent = PendingIntent.getBroadcast(
                        this@MainActivity,
                        task.id,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(resultPendingIntent)

                    reloadListView()
                }

                setNegativeButton("CANCEL", null)
            }

            val dialog = builderAlertDialog.create()
            dialog.show()

            true
        }

        reloadListView()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

    private fun reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listViewTasks.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }
}