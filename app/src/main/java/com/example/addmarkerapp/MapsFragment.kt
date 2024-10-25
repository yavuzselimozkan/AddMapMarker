package com.example.addmarkerapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.addmarkerapp.Model.Marker
import com.example.addmarkerapp.databinding.FragmentMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private var markers = ArrayList<Marker>()
    private var locationManager: LocationManager? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission()
    }

    private fun requestPermission()
    {
        locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager

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
            if(!grantResults.isEmpty() && grantResults[0],grantResults[1] == Man)
        }

        startLocationRequest()
    }

    var locationListener = LocationListener{
        //TODO
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

    }

    private fun updateShared()
    {

    }
}