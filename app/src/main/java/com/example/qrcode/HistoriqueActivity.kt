package com.example.qrcode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoriqueActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historique)

        val recyclerView: RecyclerView = findViewById(R.id.historyRecyclerView)
        val scanHistory = intent.getStringArrayListExtra("SCAN_HISTORY") ?: arrayListOf()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = HistoryAdapter(scanHistory)
    }
}
