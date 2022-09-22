package com.example.playtime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val officeBuilding : Spinner = findViewById(R.id.officeBuildingOptions)

        ArrayAdapter.createFromResource(
            this,
            R.array.office_buildings,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            officeBuilding.adapter = adapter
        }

        val playAreas : Spinner = findViewById(R.id.playAreaOptions)

        ArrayAdapter.createFromResource(
            this,
            R.array.play_areas,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            playAreas.adapter = adapter
        }

        val checkStatus = findViewById<Button>(R.id.checkStatus)

        checkStatus.setOnClickListener {
            val officeLocationSelected = officeBuilding.selectedItem.toString()
            val playAreaSelected       = playAreas.selectedItem.toString()

            val myIntent = Intent(this, StatusActivity::class.java)
            val bundle = Bundle()
            bundle.putString("officeLocation", officeLocationSelected)
            bundle.putString("playArea", playAreaSelected)
            myIntent.putExtras(bundle)

            startActivity(myIntent)
        }

    }

}