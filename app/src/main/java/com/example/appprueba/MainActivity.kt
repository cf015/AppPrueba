package com.example.appprueba

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter
import android.widget.Chronometer
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.ListView;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.app.Activity;
import android.print.PrinterId
import android.view.View.OnClickListener;
import android.os.Handler;
import android.widget.TextView;
import android.Manifest;
import android.annotation.SuppressLint
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat
import org.w3c.dom.Text
import android.os.Build
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Vector




class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    var crono: Chronometer? = null
    private var iniciado = false
    private var valorTotal = 0.0
    //localizaqacion del cliente por parte de android
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    //permisos para poder acceder a la ubicacion del celular
    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?)= false
    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        //Base de datos SqLite.
        botonGuardar.setOnClickListener {
            val admin = SqLiteAdmin(this,"administracion", null, 1)
            val bd = admin.writableDatabase
            val registro = ContentValues()
            //registro.put("codigo", input1.getText().toString())
            registro.put("nombre", input2.getText().toString())
            registro.put("total", valorTotal.toString())
            bd.insert("Registros", null, registro)
            bd.close()
            input2.setText("")
            Toast.makeText(this, "Se guardaron los datos del artículo", Toast.LENGTH_SHORT).show()
        }
        //

        //BotonConsultar
        botonConsultar.setOnClickListener {
            val admin = SqLiteAdmin(this, "administracion", null, 1)
            val bd = admin.writableDatabase
            val fila = bd.rawQuery("select nombre,total from Registros where nombre='${input2.text.toString()}'", null)
            if (fila.moveToFirst()) {
                nombreCliente.setText(fila.getString(0))
                pagoConsultar.setText(fila.getString(1))
            } else
                Toast.makeText(this, "No existe un artículo con dicho código",  Toast.LENGTH_SHORT).show()
            bd.close()
        }

        //

        //Maps.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        var button: Button
        var buttonFuncion: Button
        crono = findViewById(R.id.chronometer1) as Chronometer

        button = findViewById(R.id.btn_cronometro) as Button
        buttonFuncion = findViewById(R.id.btn_cronometroStop) as Button
        buttonFuncion.setOnClickListener(detener)
        button.setOnClickListener(empezar)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
    }

    //Marquer
    private fun placeMarker(location:  LatLng){
        val markerOption = MarkerOptions().position(location)
        mMap.addMarker(markerOption)
    }

    //funcion para el permiso
    private fun setUpMap(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true;
        //dibuja ya la ubicacion en la mapa
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if (location != null){
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarker(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }




    var empezar: View.OnClickListener = object : OnClickListener {
        override fun onClick(v: View) {
            var button: Button
            button = findViewById(R.id.btn_cronometro) as Button

            if (!iniciado) {
                crono?.setBase(SystemClock.elapsedRealtime())
                crono?.start()
                iniciado = true;
                button.setText("Detener");
            } else {
                crono?.stop();
                iniciado = false;
                button.setText("Iniciar");
            }

        }

    }

    //var detener: View.OnClickListener = OnClickListener { crono?.stop() }
    var detener: View.OnClickListener = object : OnClickListener {
        override fun onClick(v: View) {
            crono?.onChronometerTickListener = Chronometer.OnChronometerTickListener { chronometer ->
                val systemCurrTime = SystemClock.elapsedRealtime()
                val chronometerBaseTime = chronometer.base
                val deltaTime = systemCurrTime - chronometerBaseTime
                var valor = 50;
                // valorTotal = 1000;
                println(deltaTime);

                if(deltaTime > 1000){
                    var textView: TextView
                    textView = findViewById(R.id.pagoTotal) as TextView
                    valorTotal = deltaTime * 0.05 / 10000;
                    println(valorTotal);
                    textView.setText("Valor = '$valorTotal'");
                    //chronometer.base = systemCurrTime
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_cronometro, menu)
        return true
    }
}