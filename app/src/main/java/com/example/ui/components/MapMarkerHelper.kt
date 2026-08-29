package com.example.ui.components

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.data.CommunityPlaceType

object MapMarkerHelper {

    fun createMarkerDrawable(context: Context, type: CommunityPlaceType, isSelected: Boolean): Drawable {
        val density = context.resources.displayMetrics.density
        val baseWidth = if (isSelected) (54 * density).toInt() else (44 * density).toInt()
        val baseHeight = if (isSelected) (64 * density).toInt() else (52 * density).toInt()

        val bitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pinColor = when (type) {
            CommunityPlaceType.MOSQUE -> Color.parseColor("#059669") // Emerald
            CommunityPlaceType.HALAL_RESTAURANT -> Color.parseColor("#D97706") // Amber Orange
            CommunityPlaceType.HALAL_MARKET -> Color.parseColor("#EA580C") // Deep Orange
            CommunityPlaceType.COMMUNITY_EVENT -> Color.parseColor("#2563EB") // Sapphire Blue
        }

        val centerX = baseWidth / 2f
        val headRadius = (baseWidth / 2f) - (4 * density)
        val headCenterY = headRadius + (3 * density)
        val tipY = baseHeight - (2 * density)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = pinColor
            setShadowLayer(
                if (isSelected) 8 * density else 4 * density,
                0f,
                2 * density,
                Color.argb(100, 0, 0, 0)
            )
        }

        // Draw pin teardrop path
        val path = Path().apply {
            arcTo(
                RectF(centerX - headRadius, headCenterY - headRadius, centerX + headRadius, headCenterY + headRadius),
                -180f,
                180f,
                false
            )
            lineTo(centerX, tipY)
            close()
        }
        canvas.drawPath(path, paint)

        // If selected, draw accent stroke
        if (isSelected) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3 * density
                color = Color.WHITE
            }
            canvas.drawPath(path, strokePaint)
        }

        // Inner white circle
        val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val innerRadius = headRadius * 0.68f
        canvas.drawCircle(centerX, headCenterY, innerRadius, innerCirclePaint)

        // Draw icon/symbol in center
        val symbol = when (type) {
            CommunityPlaceType.MOSQUE -> "🕌"
            CommunityPlaceType.HALAL_RESTAURANT -> "🍽️"
            CommunityPlaceType.HALAL_MARKET -> "🥩"
            CommunityPlaceType.COMMUNITY_EVENT -> "📅"
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = innerRadius * 1.05f
            textAlign = Paint.Align.CENTER
        }
        val textBounds = Rect()
        textPaint.getTextBounds(symbol, 0, symbol.length, textBounds)
        val textY = headCenterY + (textBounds.height() / 2f) - (1 * density)

        canvas.drawText(symbol, centerX, textY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun createUserLocationDrawable(context: Context): Drawable {
        val density = context.resources.displayMetrics.density
        val size = (36 * density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = size / 2f

        // Outer halo
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(60, 37, 99, 235)
        }
        canvas.drawCircle(center, center, center - (2 * density), haloPaint)

        // White border
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            setShadowLayer(4 * density, 0f, 2 * density, Color.argb(80, 0, 0, 0))
        }
        canvas.drawCircle(center, center, 10 * density, whitePaint)

        // Blue center dot
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#2563EB")
        }
        canvas.drawCircle(center, center, 7 * density, dotPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
