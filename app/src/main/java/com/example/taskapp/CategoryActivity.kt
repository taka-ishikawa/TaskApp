package com.example.taskapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.content_category.*
import kotlinx.android.synthetic.main.content_category.add_category_button
import kotlinx.android.synthetic.main.content_edit.*
import java.util.*

class CategoryActivity : AppCompatActivity() {

    private lateinit var category: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(toolbar2)

        add_category_button.setOnClickListener {
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            val category = Category()

            val categoryRealmResults = realm.where(Category::class.java).findAll()
            val identifier: Int =
                if (categoryRealmResults.max("categoryId") != null) { //there are some results
                    categoryRealmResults.max("categoryId")!!.toInt() + 1
                } else { //there is no result
                    0
                }

            category.strCategory = category_category_edit_text.text.toString()
            category.categoryId = identifier

            realm.copyToRealmOrUpdate(category)
            realm.commitTransaction()
            realm.close()

            Toast.makeText(this, "カテゴリを追加しました", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
