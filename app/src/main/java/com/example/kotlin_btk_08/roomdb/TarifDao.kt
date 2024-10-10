package com.example.kotlin_btk_08.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.kotlin_btk_08.model.Tarif
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface TarifDao {
    @Query ("SELECT * FROM Tarif")
     fun getAll() : Flowable<List<Tarif>>

     @Query ("SELECT * FROM Tarif WHERE id = :tarifId")
     fun findById(tarifId: Int) : Flowable< Tarif>

     @Insert
     fun insert(tarif: Tarif) : Completable

     @Delete
     fun delete(tarif: Tarif) :Completable


}