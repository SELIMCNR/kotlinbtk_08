package com.example.kotlin_btk_08.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kotlin_btk_08.model.Tarif


@Database(entities = [Tarif::class], version = 1)
abstract class TarifDatabase : RoomDatabase()
{
    abstract fun tarifDao() : TarifDao

}