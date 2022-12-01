package com.shah.funprimeapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.shah.funprimeapp.databinding.SingleItemBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class Adapter(private val bitmapArray: ArrayList<Bitmap>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mInterstitialAd: InterstitialAd? = null



    inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SingleItemBinding
        init {
            binding = SingleItemBinding.bind(itemView)
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
             val view = LayoutInflater.from(parent.context).inflate(R.layout.single_item, parent, false)
             return myViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentImg = bitmapArray[position]



            val holderImg = holder as myViewHolder
            Glide
                .with(context)
                .load(currentImg)
                .into(holderImg.binding.img);

            holder.itemView.setOnClickListener {
                val name = "img$position"

                if (position % 2 == 0) prepareAd()

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveImage(currentImg, name)
                } else {
                    requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    );
                }


            }


    }

    override fun getItemCount(): Int {
        return bitmapArray.size
    }


    fun prepareAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    showAd()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    Log.d("TAG", "Ad is not ready yet")
                }
            })
    }

    private fun showAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd!!.show(context as Activity)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
            prepareAd()
        }
    }

    fun saveImage(currentImg: Bitmap, name: String) {
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        } else {
            val imageDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imageDirectory, name)
            fos = FileOutputStream(image)
        }

        fos.use {
            currentImg.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(context, "Photo Saved", Toast.LENGTH_SHORT).show()
        }

    }


}
