package ru.artembotnev.basesaver.imageTransform


import android.graphics.*

import com.squareup.picasso.Transformation

/**
 * Created by Artem Botnev on 24.01.2018.
 */

class CircleTransformation : Transformation {
    private var x: Int = 0
    private var y: Int = 0

    override fun transform(source: Bitmap?): Bitmap? {
        if (source == null) return null

        val size = Math.min(source.width, source.height)
        x = (source.width - size) / 2
        y = (source.height - size) / 2

        val sq = Bitmap.createBitmap(source, x, y, size, size)
        if (sq !== source) {
            source.recycle()
        }

        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.shader = BitmapShader(sq, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true

        val rad: Float = size / 2f
        canvas.drawCircle(rad, rad, rad, paint)

        sq.recycle()

        return result
    }

    override fun key() = "x = $x, y = $y"
}