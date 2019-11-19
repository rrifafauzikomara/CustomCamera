package com.rifafauzi.customcamerasurfaceview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rifafauzi.customcamerasurfaceview.R
import com.rifafauzi.customcamerasurfaceview.model.TextModel

/**
 * Created by rrifafauzikomara on 2019-11-19.
 */

class TextAdapter(private val context: Context, private val textModels: List<TextModel>) : RecyclerView.Adapter<TextAdapter.TextViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        return TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text, parent, false))
    }

    override fun getItemCount(): Int = textModels.size

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.text1.text = textModels[position].id.toString()
        holder.text2.text = textModels[position].text
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1 = itemView.findViewById<TextView>(R.id.item_text_recognition_text_view1)!!
        val text2 = itemView.findViewById<TextView>(R.id.item_text_recognition_text_view2)!!
    }

}