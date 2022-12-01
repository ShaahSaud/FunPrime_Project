package com.shah.funprimeapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.ads.nativetemplates.rvadapter.AdmobNativeAdAdapter
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.shah.funprimeapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var binding: ActivityMainBinding
    private lateinit var ImgDrawable: Drawable
    var bitmapArray = ArrayList<Bitmap>()
    var imgPath: String = ""
    private var imageResource: Int = 0
    private lateinit var layoutManager :GridLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.txt_value_config)
        getRemoteConfigValue()


        for (i in 1..10) {
            imgPath = "@drawable/img$i"
            imageResource = resources.getIdentifier(imgPath, null, packageName)
            ImgDrawable = resources.getDrawable(imageResource)



            drawableToBitmap(ImgDrawable)?.let { bitmapArray.add(it) }


        }




        val adapter =Adapter(bitmapArray, this)
        val admobNativeAdAdapter = AdmobNativeAdAdapter.Builder.with(
            resources.getString(R.string.native_id),
            adapter,
            "medium"
        ).adItemInterval(3).build()
        binding.recView.adapter = admobNativeAdAdapter







    }

    private fun getRemoteConfigValue() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    val updatedText = remoteConfig.getString("txt_remote_config")
                    binding.textView.text = updatedText
                } else {
                    Toast.makeText(
                        this, "Fetch failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }
}