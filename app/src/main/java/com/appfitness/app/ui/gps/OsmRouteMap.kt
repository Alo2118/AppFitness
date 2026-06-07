package com.appfitness.app.ui.gps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

/** A lat/lon coordinate for the map (kept independent from osmdroid types). */
typealias LatLon = Pair<Double, Double>

/**
 * Draws a GPS route on an OpenStreetMap map (osmdroid — no API key / Play
 * Services). [fitBounds] zooms to show the whole route (history view); when
 * false the map follows the latest point (live tracking view).
 */
@Composable
fun OsmRouteMap(
    points: List<LatLon>,
    modifier: Modifier = Modifier,
    fitBounds: Boolean = true,
    lineColor: Int = 0xFF2E7D32.toInt(),
) {
    val context = LocalContext.current

    val mapView = androidx.compose.runtime.remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()
            if (points.isEmpty()) {
                map.invalidate()
                return@AndroidView
            }
            val geo = points.map { GeoPoint(it.first, it.second) }

            if (geo.size >= 2) {
                map.overlays.add(
                    Polyline(map).apply {
                        setPoints(geo)
                        outlinePaint.color = lineColor
                        outlinePaint.strokeWidth = 10f
                    }
                )
                addMarker(map, geo.first(), "Partenza")
                addMarker(map, geo.last(), "Arrivo")
            }

            if (fitBounds && geo.size >= 2) {
                val box = BoundingBox.fromGeoPoints(geo)
                map.post { runCatching { map.zoomToBoundingBox(box, true, 80) } }
            } else {
                map.controller.setCenter(geo.last())
                if (!fitBounds) map.controller.setZoom(17.0)
            }
            map.invalidate()
        },
    )
}

private fun addMarker(map: MapView, point: GeoPoint, title: String) {
    map.overlays.add(
        Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
        }
    )
}
