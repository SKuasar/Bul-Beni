package com.example.deneme4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class GirisActivity extends AppCompatActivity {

    private TextInputLayout tilEmail,tilSifre,tilKullaniciAdi,tilKullaniciID,tilKullaniciTel;
    private TextInputEditText email,sifre,kullaniciadi,kullaniciID,kullaniciTEL;
    private ProgressBar progresBarCircle;
    private ImageView profilPhoto;
    private Uri profilPhotoUri = null;
    private static final int RESIM_SEC = 1;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private DatabaseReference databaseReference;
    private ValueEventListener databaseEventListener;
    private boolean profildegisecek = false;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){         //kullanıcı değeri varsa progress barı aktif et ve kullanıcıyı yerine yazdır
            progresBarCircle.setVisibility(View.VISIBLE);
            updateUI(user);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(databaseReference!=null && databaseEventListener!=null)
            databaseReference.removeEventListener(databaseEventListener);
    }

    private void updateUI(FirebaseUser user) {
        if(user == null){       //kullanıcı değeri boşsa her şeyi 0 la
            kullaniciadi.setText(null);
            profilPhoto.setImageResource(R.drawable.images);
            email.setText(null);
            sifre.setText(null);
            return;
        }

        if(databaseReference == null){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user.getUid());
        }
        ((Button)findViewById(R.id.buttonGiris)).setText("Takip Listene Git");

        if(profildegisecek){
            progresBarCircle.setVisibility(View.GONE);
            return;
        }

        databaseEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progresBarCircle.setVisibility(View.GONE);

                String ad = snapshot.child("kullanici_adi").getValue(String.class);
                String url = snapshot.child("kullanici_url").getValue(String.class);
                String ID = snapshot.child("kullanici_id").getValue(String.class);
                String tel = snapshot.child("kullanici_tel").getValue(String.class);
                kullaniciadi.setText(ad);
                kullaniciTEL.setText(tel);
                //Bu araya kullanıcı idyi de ekleyip takipleşme işlemini öle yap
                kullaniciID.setText(ID);
                Picasso.get().load(url).into(profilPhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progresBarCircle.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris);

        mStorageRef = FirebaseStorage.getInstance().getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        tilEmail = findViewById(R.id.tilEmail);
        email = findViewById(R.id.email);
        tilSifre = findViewById(R.id.tilSifre);
        sifre = findViewById(R.id.sifre);
        tilKullaniciAdi = findViewById(R.id.tilKullaniciAdi);
        kullaniciadi = findViewById(R.id.kullaniciadi);
        progresBarCircle = findViewById(R.id.progresBarCircle);
        profilPhoto = findViewById(R.id.profil);
        tilKullaniciID = findViewById(R.id.tilKullaniciID);
        kullaniciID = findViewById(R.id.kullaniciID);
        tilKullaniciTel = findViewById(R.id.tilKullaniciTel);
        kullaniciTEL = findViewById(R.id.kullaniciTEL);

        if(mAuth.getCurrentUser() != null){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(mAuth.getCurrentUser().getUid());
        }

        profilPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(intent2,RESIM_CEK);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Resim Seçiniz"),RESIM_SEC);
                profildegisecek=true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RESIM_SEC && resultCode==RESULT_OK && data!=null){

            profilPhotoUri=data.getData();
            profilPhoto.setImageURI(null);
            profilPhoto.setImageURI(profilPhotoUri);

            //Bitmap bitmap = {Bitmap} data.getExtras().get("data");
            //profilPhoto.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,Menu.NONE,"Çıkış").setIcon(R.drawable.ic_baseline_exit_to_app_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        mAuth.signOut();
        updateUI(null);
        return super.onOptionsItemSelected(item);
    }

    public void profilKaydet(View view) {


        tilKullaniciAdi.setError(null);
        if(profilPhotoUri==null || TextUtils.isEmpty(kullaniciadi.getText())){
            if(profilPhotoUri == null){
                Toast.makeText(this,"Lütfen profil fotoğrafınızı belirleyiniz",Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(kullaniciadi.getText()))
                tilKullaniciAdi.setError("Lütfen kullanıcı adını giriniz");

            return;
        }
        progresBarCircle.setVisibility(View.VISIBLE);

        String kullaniciAdi=kullaniciadi.getText().toString();
        String kullaniciTel = kullaniciTEL.getText().toString();
        String uzanti = getFileExtension(profilPhotoUri);

        StorageReference childRef = mStorageRef.child("KullaniciProfili").child(mAuth.getCurrentUser().getUid()).child(kullaniciAdi+"."+uzanti);
        childRef.putFile(profilPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference.child("kullanici_adi").setValue(kullaniciAdi);
                        databaseReference.child("kullanici_id").setValue(mAuth.getCurrentUser().getUid());
                        databaseReference.child("kullanici_url").setValue(uri.toString());
                        databaseReference.child("kullanici_tel").setValue(kullaniciTel);
                        profildegisecek=false;
                    }
                });

                Toast.makeText(getApplicationContext(), "Foto kayıt başarılı", Toast.LENGTH_SHORT).show();
                progresBarCircle.setVisibility(View.GONE);
            }
        });

    }

    private String getFileExtension(Uri uri){     //resim dosyasının uzantısının ne olduğunu belirleyen  fonksiyon
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap =MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void kayitOl(View view) {
        if(!validateForm()){
            return;
        }

        progresBarCircle.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email.getText().toString(),sifre.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(getApplicationContext(), "Kayıt başarılı.",
                            Toast.LENGTH_SHORT).show();
                    ((Button)findViewById(R.id.buttonGiris)).setText("Takip Listene Git");
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(), "Kayıt başarısız.",
                            Toast.LENGTH_SHORT).show();
                    Log.w("LOG", "createUserWithEmail:failure", task.getException());
                }
                    progresBarCircle.setVisibility(View.GONE);
            }
        });
    }

    public void girisYap(View view) {


        if(mAuth.getCurrentUser() != null){
                startActivity(new Intent(this,MainActivity.class));
                finish();
                return;
        }

        if(!validateForm())
            return;

        progresBarCircle.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email.getText().toString(),sifre.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progresBarCircle.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                    Toast.makeText(getApplicationContext(),"Giris Başarılı",Toast.LENGTH_SHORT).show();
                    ((Button)findViewById(R.id.buttonGiris)).setText("Takip Listene Git");
                }
                else{
                    Toast.makeText(getApplicationContext(),"Giris Başarısız",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private boolean validateForm() {
        boolean valid = true;
        tilEmail.setError(null); //hata mesajlarını siler
        sifre.setError(null);

        if(TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(sifre.getText())){  //e mail ve sifre girilmemişse
            if (TextUtils.isEmpty(email.getText())){    //e mail bos ise
                tilEmail.setError("Lütfen e-mail adresinizi giriniz");
                valid = false;
            }
            else{
                if(!email.getText().toString().contains("@")){
                    tilEmail.setError("Lütfen geçerli bir e-posta adresi giriniz");
                    valid = false;
                }
            }
        }

        if(TextUtils.isEmpty(sifre.getText())){
            tilSifre.setError("Lütfen şifrenizi giriniz");
            valid = false;
        }

        return valid;
    }

}