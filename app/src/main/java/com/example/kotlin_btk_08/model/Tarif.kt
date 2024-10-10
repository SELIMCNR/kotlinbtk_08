package com.example.kotlin_btk_08.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Tarif (

    @ColumnInfo (name = "isim")
    val isim: String,

    @ColumnInfo (name = "malzeme")
    val malzeme: String,

    @ColumnInfo (name = "gorsel")
    val gorsel: ByteArray
)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0
}