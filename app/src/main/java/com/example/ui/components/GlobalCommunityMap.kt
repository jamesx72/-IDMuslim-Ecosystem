package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun GlobalCommunityMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Set User Agent for osmdroid (required by OSM policies)
    val userAgent = "IDMuslimApp/1.0"
    Configuration.getInstance().userAgentValue = userAgent

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            
            // Set initial map position to focus on the Middle East / Global view
            val mapController = this.controller
            mapController.setZoom(2.5)
            val startPoint = GeoPoint(30.0, 30.0)
            mapController.setCenter(startPoint)

            // Disable repeat to make it look cleaner (optional)
            // isTilesScaledToDpi = true
            
            // Add some markers representing the density of users globally
            addDensityMarker(this, 21.4225, 39.8262, "Mecca - 25k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 48.8566, 2.3522, "Paris - 12k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 51.5074, -0.1278, "London - 18k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 37.7749, -122.4194, "San Francisco - 5k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, -6.2088, 106.8456, "Jakarta - 85k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 6.5244, 3.3792, "Lagos - 40k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 25.2048, 55.2708, "Dubai - 30k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 41.0082, 28.9784, "Istanbul - 55k Users", 0xFF0F5A47.toInt())
            addDensityMarker(this, 3.1390, 101.6869, "Kuala Lumpur - 45k Users", 0xFF0F5A47.toInt())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

private fun addDensityMarker(mapView: MapView, lat: Double, lon: Double, title: String, colorInt: Int) {
    val marker = Marker(mapView)
    marker.position = GeoPoint(lat, lon)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    marker.title = title
    // Optional: set custom icon or just use default but colorize if possible, 
    // osmdroid defaults to a standard pin. 
    mapView.overlays.add(marker)
    
    // We can also add a semi-transparent circle to represent "density radius"
    val circle = Polygon().apply {
        points = Polygon.pointsAsCircle(GeoPoint(lat, lon), 500000.0) // 500km radius
        fillPaint.color = 0x550F5A47 // Semi-transparent emerald green
        fillPaint.style = android.graphics.Paint.Style.FILL
        outlinePaint.color = 0xAA0F5A47.toInt()
        outlinePaint.strokeWidth = 2f
    }
    mapView.overlays.add(circle)
}
