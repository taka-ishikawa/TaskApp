package com.example.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

class Category: RealmObject() {
    var strCategory: String = ""
    @PrimaryKey
    var categoryId: Int = 0
}