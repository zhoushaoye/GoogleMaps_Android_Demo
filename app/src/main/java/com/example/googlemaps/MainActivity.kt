package com.example.googlemaps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var locationCallback: LocationCallback
    //用户当前位置
    private var currentLatLng: LatLng = LatLng(1.0, 1.0)
    //用户目的地
    private var destinationLatLng: LatLng = LatLng(1.0, 1.0)
    companion object {
        private const val API_KEY =
            "AIzaSyCYEjZVnDQWY01I6XMdQq5pj8FXsvu2V28"
        private val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMap()

        initFusedLocationClient()

        // 检查权限
        initPermission()

        // 初始化 LocationCallback
        initLocationCallback()

        // 初始化导航点击事件
        initNavigationBtnClickListener()
    }

    private fun initNavigationBtnClickListener() {
        findViewById<Button>(R.id.startButton).setOnClickListener {
            // 获取路线信息和显示路径
            showPath(currentLatLng, destinationLatLng)
        }
    }

    private fun initFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 请求权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 已经有权限，获取位置
            requestLocation()
        }
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // 处理位置变化
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    // 在地图上显示当前位置
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("当前位置"))
                    // 将地图视图移到当前位置
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // 在地图上显示目标位置
        googleMap.addMarker(MarkerOptions().position(destinationLatLng).title("目的地"))
        // 将地图视图移到目标位置
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLatLng))
        // 点击地图添加标记
        googleMap.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }
    }

    private fun addMarker(latLng: LatLng) {
        // 添加标记
        val marker: Marker? =
            googleMap.addMarker(MarkerOptions().position(latLng).title("New Marker"))
        marker?.showInfoWindow() // 显示标记信息窗口
        // 移动地图视图到标记位置
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        // 设置标记点击监听器
        googleMap.setOnMarkerClickListener { clickedMarker ->
            // 获取标记的坐标
            destinationLatLng = clickedMarker.position
            // 在此处处理点击标记的逻辑
            true
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // 处理位置信息
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)
                    // 在地图上显示当前位置
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("当前位置"))
                    // 将地图视图移到当前位置
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                    // 开始监听位置变化
                    startLocationUpdates()
                }
            }
            .addOnFailureListener { e ->
                // 处理定位失败的情况
            }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1000) // 更新间隔，单位毫秒

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showPath(start: LatLng, end: LatLng) {
        // 获取路线信息
        GlobalScope.launch(Dispatchers.Main) {
            val directionsResult = getDirectionsResult(start, end)
            // 显示路径
            drawPath(directionsResult)
        }
    }

    private suspend fun getDirectionsResult(start: LatLng, end: LatLng): DirectionsResult {
        return withContext(Dispatchers.IO) {
            val context = GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build()
            DirectionsApi.newRequest(context)
                .origin(com.google.maps.model.LatLng(start.latitude, start.longitude))
                .destination(com.google.maps.model.LatLng(end.latitude, end.longitude))
                .mode(TravelMode.DRIVING)
                .await()
        }
    }

    private fun drawPath(directionsResult: DirectionsResult) {
        val polylineOptions = PolylineOptions()
        val legs = directionsResult.routes[0].legs
        for (leg in legs) {
            for (step in leg.steps) {
                // 获取每个路段的耗时
                val duration = step.duration
                val durationText = duration.humanReadable
                // 画路径
                val points = step.polyline.decodePath()
                for (point in points) {
                    polylineOptions.add(LatLng(point.lat, point.lng))
                }
            }
        }

        // 设置路径样式
        polylineOptions.width(5f)
            .color(
                ContextCompat.getColor(
                    this,
                    androidx.appcompat.R.color.primary_dark_material_dark
                )
            )

        // 在地图上显示路径
        googleMap.addPolyline(polylineOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了位置权限
                requestLocation()
            } else {
                // 用户拒绝了位置权限
                // 在这里可以添加适当的处理代码，例如显示一条消息说明为什么需要位置权限
            }
        }
    }
}