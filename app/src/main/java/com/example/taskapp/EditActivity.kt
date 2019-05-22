package com.example.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_edit.*
import java.util.*

class EditActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // ActionBarを設定する
        setSupportActionBar(toolbar)
        if (supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)
        add_category_button.setOnClickListener(mOnAddClickListener)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)

        if (mTask != null) {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
//            category_edit_text.setText(mTask!!.category)

            calendar.time = mTask!!.date

            //why mMonth '+ 1'. so it is because it is so.
            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    override fun onResume() {
        super.onResume()
        //search by category
//        val categoryAdapter = CategoryAdapter(this)
        val categoryRealmResults = realm.where(Category::class.java).findAll()
        val categoryList = ArrayList<String>()
        for (i in 1 .. categoryRealmResults.size) {
            categoryList.add(categoryRealmResults[i -1]?.strCategory.toString())
        }

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
        category_spinner.adapter = categoryAdapter
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                mHour = hourOfDay
                mMinute = minute
                val timeString = String.format("%02d", mHour) + "/" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        if (category_spinner.selectedItem == null) {
            Snackbar.make(it, "カテゴリを追加してください", Snackbar.LENGTH_LONG).show()
            return@OnClickListener
        } else {
            addTask()
            finish()
        }
    }

    private val mOnAddClickListener = View.OnClickListener {
        val intent = Intent(this, CategoryActivity::class.java)
        startActivity(intent)
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) { //there are some results
                    taskRealmResults.max("id")!!.toInt() + 1
                } else { //there is no result
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val strCategory = category_spinner.selectedItem.toString()
        val category = realm.where(Category::class.java).equalTo("strCategory", strCategory)?.findFirst()

        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category = category
        val calender = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calender.time
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()
        realm.close()

        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calender.timeInMillis, resultPendingIntent)

    }
}
