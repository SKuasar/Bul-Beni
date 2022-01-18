package com.example.deneme4;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.deneme4.databinding.ActivityMapsBinding;
import com.squareup.picasso.Picasso;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double enlem,boylam;
    private String kullaniciAdi,profilUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        enlem = getIntent().getDoubleExtra("enlem",0);
        boylam = getIntent().getDoubleExtra("boylam",0);
        kullaniciAdi = getIntent().getStringExtra("kullanici_adi");
        profilUrl = getIntent().getStringExtra("profil_url");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng takipEdilen = new LatLng(enlem, boylam);
        mMap.addMarker(new MarkerOptions().position(takipEdilen).title(kullaniciAdi).snippet("burdayim").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(takipEdilen));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(takipEdilen,10));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //harita görünümü
        mMap.setMyLocationEnabled(true);  //kendi konumunu göstersin mi

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout layout = new LinearLayout(MapsActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT.LinearLayout.LayoutParams.Wra)

                ImageView profil = new ImageView(MapsActivity.this);
                //profil.setScaleType(ScaleType.CENTER_CROP);
                TextView isim = new TextView(MapsActivity.this);
                TextView aciklama = new TextView(MapsActivity.this);

                Picasso.get().load(profilUrl).into(profil);
                isim.setText(marker.getTitle());
                isim.setTextColor(Color.BLACK);
                isim.setGravity(Gravity.CENTER_HORIZONTAL);
                aciklama.setText(marker.getSnippet());
                aciklama.setGravity(Gravity.CENTER_HORIZONTAL);

                layout.addView(profil,300,300);
                layout.addView(isim);
                layout.addView(aciklama);

                return layout;
            }
        });
    }
}