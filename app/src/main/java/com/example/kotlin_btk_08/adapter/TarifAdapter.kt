package com.example.kotlin_btk_08.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_btk_08.databinding.RecyclerRowBinding
import com.example.kotlin_btk_08.model.Tarif
import com.example.kotlin_btk_08.view.ListeFragmentDirections

class TarifAdapter(val tarifListesi: List<Tarif>): RecyclerView.Adapter<TarifAdapter.TarifHolder>() {
    class TarifHolder (val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)   {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  TarifHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
        val bitmap = BitmapFactory.decodeByteArray(tarifListesi.get(position).gorsel,0,tarifListesi.get(position).gorsel.size)
        holder.binding.imageView.setImageBitmap(bitmap)
        holder.binding.recyclerViewTextView.text = tarifListesi[position].isim
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(id = tarifListesi[position].id,"eski")
            Navigation.findNavController(it).navigate(action)
        }
    }



}