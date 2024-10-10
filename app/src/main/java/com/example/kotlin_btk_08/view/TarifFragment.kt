package com.example.kotlin_btk_08.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.kotlin_btk_08.databinding.FragmentTarifBinding
import com.example.kotlin_btk_08.model.Tarif
import com.example.kotlin_btk_08.roomdb.TarifDao
import com.example.kotlin_btk_08.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {

    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var secilenGorsel: Uri? =null   //galeriden gelen
    private var secilenBitmap : Bitmap? = null  //galeriden gelen

    private  lateinit var  db : TarifDatabase
    private  lateinit var  tarifDao : TarifDao

    private  val mDisposable = CompositeDisposable()
    private var secilenTarif : Tarif? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler")
            .allowMainThreadQueries() // bu kullanılabilir ancak riskli
            .build()
        tarifDao=db.tarifDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
 }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButton.setOnClickListener{
            gorselSec(it)
        }
        binding.kaydetButton.setOnClickListener{
            kaydet(it)
        }
        binding.silButton.setOnClickListener{
            sil(it)
        }

        arguments?.let {
            val gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (gelenBilgi == "yeni"){
                //yeni yemek eklemeye geldi
                secilenTarif = null

                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.editisimText.setText("")
                binding.editMalzemeText2.setText("")
            }
            else{

                //var olan yemeği düzenlemeye geldi
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = false
                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )

            }
        }
    }
    private  fun handleResponse(tarif:Tarif){
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageButton.setImageBitmap(bitmap)

        binding.editisimText.setText(tarif.isim)
        binding.editMalzemeText2.setText(tarif.malzeme)
       secilenTarif = tarif
    }
    fun sil (view: View){
        if (secilenTarif!=null){
            mDisposable.add(
                tarifDao.delete(secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert ))
        }
        else {

        }

    }
    fun kaydet (view: View) {
        val isim = binding.editisimText.text.toString()
        val malzeme = binding.editMalzemeText2.text.toString()

        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, maximumBoyut = 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()
            val tarif = Tarif(isim,malzeme,byteArray)
          //  tarifDao.insert(tarif)

            // Threading
            //RxJava
            tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert)

        }
    }

    private fun handleResponseForInsert (){
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun gorselSec (view: View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                // izin verilmemiş , izin istememiz gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES )){
                    // snackbar göstermemiz lazım , izin istememiz gerekiyor
                    Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",View.OnClickListener {
                            // izin istememiz gerekiyor


                            // izin isteme
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES )
                        }
                    ).show()

                }
                else {
                    // izin istememiz gerekiyor
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }

            }
            else{
                // izin zaten verilmiş , galeriye git
                //galeriye gidebiliriz
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        }
        else{
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                // izin verilmemiş , izin istememiz gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE )){
                    // snackbar göstermemiz lazım , izin istememiz gerekiyor
                    Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",View.OnClickListener {
                            // izin istememiz gerekiyor


                            // izin isteme
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE )
                        }
                    ).show()

                }
                else {
                    // izin istememiz gerekiyor
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }

            }
            else{
                // izin zaten verilmiş , galeriye git
                //galeriye gidebiliriz
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }


    }



    @SuppressLint("SuspiciousIndentation")
    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {   result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK)
            {
                val intentFromResult = result.data
                if (intentFromResult != null)
                {
                 secilenGorsel=intentFromResult.data
                    try {

                        if (Build.VERSION.SDK_INT>=28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageButton.setImageBitmap(secilenBitmap)


                        }
                        else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageButton.setImageBitmap(secilenBitmap)

                        }
                    }
                    catch (e:Exception){
                        println(e.localizedMessage)
                    }

                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result ->
            if (result){
                //izin verildi
                //galeriye gidebiliriz
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
            else{
                //izin verilmedi
                Toast.makeText(requireContext(),"İzin verilmedi!",Toast.LENGTH_LONG).show()

            }

        }

    }


    private fun kucukBitmapOlustur(kullanicininSectigiBitmap:Bitmap,maximumBoyut:Int):Bitmap{
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani :Double = width.toDouble() / height.toDouble()
        if (bitmapOrani >1){
            //görsel yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()

        }
        else {
            //görsel dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()

        }


        return  Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}