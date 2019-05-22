package com.example.taskapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.content_category.*
import kotlinx.android.synthetic.main.content_category.add_category_button
import kotlinx.android.synthetic.main.content_edit.*
import java.util.*

class CategoryActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(toolbar2)

        realm = Realm.getDefaultInstance()

        add_category_button.setOnClickListener {
            if (category_category_edit_text.text.toString() != "") {
                val category = Category()

                val categoryRealmResults = realm.where(Category::class.java).findAll()
                val identifier: Int =
                    if (categoryRealmResults.max("categoryId") != null) { //there are some results
                        categoryRealmResults.max("categoryId")!!.toInt() + 1
                    } else { //there is no result
                        0
                    }

                val categoryExistOrNot = realm.where(Category::class.java)
                    .equalTo("strCategory", category_category_edit_text.text.toString()).findFirst()
                if (categoryExistOrNot == null) { // category written is not exist
                    category.strCategory = category_category_edit_text.text.toString()
                    category.categoryId = identifier

                    realm.beginTransaction()
                    realm.copyToRealmOrUpdate(category)
                    realm.commitTransaction()

                    Toast.makeText(this, "カテゴリを追加しました", Toast.LENGTH_SHORT).show()
                    finish()
                } else { // category written has been already registered
                    Snackbar.make(it, "このカテゴリは登録済みです", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            } else Snackbar.make(it, "カテゴリを入力してください", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }
}
