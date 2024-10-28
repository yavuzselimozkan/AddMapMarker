package com.example.addmarkerapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.addmarkerapp.Model.Marker
import com.example.addmarkerapp.databinding.FragmentMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private var markers = ArrayList<Marker>()
    private var locationManager: LocationManager? = null
    private lateinit var mMap : GoogleMap

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        loadMarkers()
        mMap = googleMap

        //lokasyonu mavi olarak gösterecek
        googleMap.isMyLocationEnabled = true

        //en son konumu gösterecek
        locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
            val location = LatLng(it.latitude,it.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15f))
        }

        //marker oluşturulacak
        googleMap.setOnMapClickListener {
            val marker = Marker()
            marker.location = it
            showDetail(marker,true)
        }

        //markera basıldığında detay gösterecek
        googleMap.setOnMarkerClickListener {
            //burada her bir marker ın tag ını index olarak aldık.
            showDetail(markers[it.tag as Int],false)
            //true dönerek basma işleminin tamamlandığını gösterir ve tag gözükmemiş olur.
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager

        requestPermission()
    }

    private fun requestPermission()
    {

        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            startLocationRequest()
        }
        else
        {
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),1)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationRequest()
    {
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0.1f,locationListener)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1)
        {
            val fineLocationGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            val coarseLocationGranted = grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED

            if (fineLocationGranted && coarseLocationGranted)
            {
                startLocationRequest()
            }
            else
            {
                Toast.makeText(requireContext(),"İzin Verilmedi.",Toast.LENGTH_LONG).show()
            }
        }
    }

    var locationListener = LocationListener{
        val location = LatLng(it.latitude,it.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun showDetail(marker : Marker, isNew : Boolean)
    {
        //popupMenu'ye erişmemizi sağlar.
        val v = layoutInflater.inflate(R.layout.popup_menu,null)

        //Popup Window oluşturur.
        val popDetail = PopupWindow(v,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true)
        popDetail.showAtLocation(v, Gravity.TOP,0,0)

        val etName: EditText = v.findViewById(R.id.etName)
        etName.setText(marker.name)

        val etDetail: EditText = v.findViewById(R.id.etDetail)
        etDetail.setText(marker.detail)

        v.findViewById<Button>(R.id.saveBtn).setOnClickListener {
            marker.name = etName.text.toString()
            marker.detail = etDetail.text.toString()

            //Eğer yeniyse marker ekleyecek.
            if(isNew)
            {
                markers.add(marker)
                addMarker(marker,markers.size-1)//haritaya marker ekleyecek
            }
            updateShared()

            popDetail.dismiss()
        }
    }

    private fun addMarker(marker: Marker, id: Int)
    {
        mMap.addMarker(MarkerOptions().position(marker.location!!))?.tag = id
    }

    private fun updateShared()
    {
        val editor = requireActivity().getSharedPreferences(requireActivity().packageName, MODE_PRIVATE).edit()
        val json = Gson().toJson(markers)
        editor.putString("markers",json)
        editor.apply()
    }

    private fun loadMarkers()
    {
        val str = requireActivity().getSharedPreferences(requireActivity().packageName, MODE_PRIVATE).getString("markers","")

        if(str!!.isNotEmpty())
        {
            val t = object:TypeToken<ArrayList<Marker>>(){}.type
            markers = Gson().fromJson(str,t)

            for(i in 0 ..<markers.size-1)
            {
                addMarker(markers[i],i)
            }
        }
    }
}