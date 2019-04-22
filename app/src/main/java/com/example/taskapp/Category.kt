package com.example.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

class Category: RealmObject(), Serializable {
    var strCategory: String = ""

    @PrimaryKey
    var categoryId: Int = 0
}