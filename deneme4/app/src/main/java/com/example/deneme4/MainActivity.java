package com.example.deneme4;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.deneme4.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int ALARM_MUHRU = 1453;
    private BottomSheetBehavior bottomSheetBehavior;
    private DatabaseReference userDatabase, takipDatabase, konumlarDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextInputLayout til , tilKontrol;
    private TextInputEditText takip_kullanici_adi , takip_kullanici_ID;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private ListView listView;
    private List<Takip> takipList;
    private TakipListesiAdapter adapter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "izin alındı", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        //textInputLayout = findViewById(R.id.toolbar);

        til = findViewById(R.id.til);
        takip_kullanici_adi = findViewById(R.id.takip_kullanici_adi);
        //Kontrol için yapılan değişiklikler
        tilKontrol = findViewById(R.id.tilKontrol);
        takip_kullanici_ID = findViewById(R.id.takip_kullanici_ID);

        listView = findViewById(R.id.takipListesi);


        View view = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        takipList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        adapter = new TakipListesiAdapter(this,takipList);
        listView.setAdapter(adapter);

        if(user == null) {
            startActivity(new Intent(this, GirisActivity.class));
            finish();
            return;
        }


        userDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        takipDatabase = FirebaseDatabase.getInstance().getReference().child("Takiplesenler");
        konumlarDatabase = FirebaseDatabase.getInstance().getReference().child("Konumlar");

        //setSupportActionBar(binding.toolbar);

        //Android M ve üzeri için lokasyon izni alma işlemi
        if(Build.VERSION.SDK_INT >= VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }


        //Takip edilenlerin listesi
        takipDatabase.orderByChild("takipEden").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                takipList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Takip takip = snapshot.getValue(Takip.class);
                    takipList.add(takip);
                }
                findViewById(R.id.progress).setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //App bar hatası var nasıl çüzüleceğini bul!!!

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        binding.fab.setOnClickListener(new View.OnClickListener() {     //binding. vardı en başta
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //((FloatingActionButton)view).hide();
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                    switch(newState){
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            fab.hide();
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            fab.show();
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            break;

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {           //Ana sayfadaki ayarlar islemi
            startActivity(new Intent(MainActivity.this,AyarActivity.class));
            return true;
        }

        if (id == R.id.action_exit) {   //ana sayfadaki çıkış işlemi
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this,GirisActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void clickEkle(View view) {
        til.setError(null);

        if(TextUtils.isEmpty(takip_kullanici_adi.getText())){
            til.setError("Kullanıcı adınızı giriniz");
            return;
        }
        if(TextUtils.isEmpty(takip_kullanici_ID.getText())){
            til.setError("Kullanıcı idnizi giriniz");
            return;
        }


        String kullaniciadi = takip_kullanici_adi.getText().toString();
        String kullanici_id_kontrol = takip_kullanici_ID.getText().toString();
         //Burda girilen id değerini alıyorum
        //String kullaniciid = takip_kullanici_ID.getText().toString();
        /*Query queryRef = userDatabase.orderByChild("kullanici_adi").equalTo(kullaniciadi);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("takipListesi",snapshot.toString());

                if(snapshot.exists()){
                    //burada ad id ve url yi firebase üzerinden alıyor
                    String ad = snapshot.child("kullanici_adi").getValue(String.class);
                    String id = snapshot.child("kullanici_id").getValue(String.class);
                    String url = snapshot.child("kullanici_url").getValue(String.class);
                    Log.d("id",id);
                    Log.d("kontrolid",kullanici_id_kontrol);
                        Toast.makeText(getApplicationContext(), ad +"kullanıcı bulundu", Toast.LENGTH_SHORT).show();
                        //takipEkle(user.getUid(),snapshot.getKey());


                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        Query queryRef2 = userDatabase.orderByChild("kullanici_id").equalTo(kullanici_id_kontrol);

        queryRef2.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("takipListesi",snapshot.toString());

                    if(snapshot.exists()){
                        //burada ad id ve url yi firebase üzerinden alıyor
                        String ad = snapshot.child("kullanici_adi").getValue(String.class);
                        String id = snapshot.child("kullanici_id").getValue(String.class);
                        String url = snapshot.child("kullanici_url").getValue(String.class);
                        //String telefon = snapshot.child("kullanici_tel").getValue(String.class);
                        Toast.makeText(getApplicationContext(), ad +"kullanıcı bulundu", Toast.LENGTH_SHORT).show();
                        takipEkle(user.getUid(),snapshot.getKey());
                    }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void takipEkle(String takipEden, String takipEdilen) {       //iki kullanıcının takipleşmesi sonrasında yapılan kayıt işlemi
        Takip takip = new Takip(takipEden, takipEdilen);
        takipDatabase.push().setValue(takip).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this,"Başarılı",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class TakipListesiAdapter extends BaseAdapter {

        private final Context context;
        private final List<Takip> takipList;

        public TakipListesiAdapter(Context c, List<Takip> takipList) {
            this.context = c;
            this.takipList = takipList;
        }

        @Override
        public int getCount() {
            return takipList.size();
        }

        @Override
        public Object getItem(int i) {
            if(takipList.size() == 0){
                return null;
            }
            return takipList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(takipList.size() == 0)
            return null;

            LinearLayout container = (LinearLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.takip_item, null);

            Takip takip = takipList.get(i);
            final TextView ad = container.findViewById(R.id.ad);
            final TextView zaman = container.findViewById(R.id.zaman);
            final TextView adresler = container.findViewById(R.id.adresler);
            final ImageView profil = container.findViewById(R.id.profilItem);
            final TextView telefonNumarasi = container.findViewById(R.id.tel);



            final Konum[] konum = new Konum[1];
            final String[] url = new String[1];

            //Profil foto ve kullanıcı adı set etme işlemi
            userDatabase.child(takip.getTakipEdilen()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String kullaniciadi = dataSnapshot.child("kullanici_adi").getValue(String.class);
                        final String kullanicitelefon = dataSnapshot.child("kullanici_tel").getValue(String.class);
                        url[0] = dataSnapshot.child("kullanici_url").getValue(String.class);
                        ad.setText(kullaniciadi);
                        telefonNumarasi.setText(kullanicitelefon);
                        Picasso.get().load(url[0]).into(profil);
                        //Arama Yapmak
                        Button call = (Button) findViewById(R.id.button);
                        call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent niyet = new Intent(Intent.ACTION_CALL);
                                niyet.setData(Uri.parse("tel:"+telefonNumarasi.getText().toString()));
                                startActivity(niyet);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            //Tarih, saat ve adres  bilgisini set etme işlemi
            konumlarDatabase.child(takip.getTakipEdilen()).orderByValue().limitToLast(1).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("Konumlar",dataSnapshot.toString());
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        konum[0] = snapshot.getValue(Konum.class);
                        setKonumBilgisi(konum[0]);
                    }
                }

                private void setKonumBilgisi(Konum konum) {

                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date date = new Date(konum.getZaman());
                    zaman.setText(format.format(date));

                    String adres="";
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                    try {
                        List<Address> addressList = geocoder.getFromLocation(konum.getEnlem(),konum.getBoylam(),1);
                        for(Address adr:addressList){
                            Log.d("Adres","setKonumBilgisi()"+adr.toString());
                            adres+=adr.getAddressLine(0);
                            for(int i=0; i<adr.getMaxAddressLineIndex();i++){

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    adresler.setText(adres);

                    /*Location location = new Location("");
                    location.setLatitude(konum.getEnlem());
                    location.setLongitude(konum.getBoylam());

                    Location mevcutKonum = new Location("");
                    location.setLatitude(enlem);
                    location.setLongitude(boylam);
                    Log.d("Konumlar","setKonumBilgisi:"+enlem+" "+boylam+" "+position+" konum"+konum.getEnlem()+ ")*/
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Mapsactivity açılacak
                    Intent intent = new Intent(context,MapsActivity.class);
                    intent.putExtra("enlem",konum[0].getEnlem());
                    intent.putExtra("boylam",konum[0].getBoylam());
                    intent.putExtra("profil_url",url[0]);
                    intent.putExtra("kullanici_adi",ad.getText().toString());
                    context.startActivity(intent);
                }
            });
            return container;

        }


    }
}