package com.example.android.electioncomission;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransation = fragmentManager.beginTransaction();
        fragmentTransation.replace(R.id.view_main, new Login());
        fragmentTransation.commit();
    }

    public void login(View v) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Spinner s = (Spinner) findViewById(R.id.spinner);
        final EditText e = (EditText) findViewById(R.id.aadhar);
        final EditText p = (EditText) findViewById(R.id.password);
        final DatabaseReference myRef = database.getReference();

        myRef.child("constituency").child(s.getSelectedItem().toString().toLowerCase()).child(e.getText().toString()).child("password").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer value = dataSnapshot.getValue(Integer.class);
                if (p.getText().toString().equals("" + value)) {
                    myRef.child("constituency").child(s.getSelectedItem().toString().toLowerCase()).child(e.getText().toString()).child("voted").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean voted = dataSnapshot.getValue(Boolean.class);
                            if (!voted) {
                                Bundle b = new Bundle();
                                b.putString("userid", e.getText().toString());
                                b.putString("constituency", s.getSelectedItem().toString());
                                FragmentTransaction fragmentTransation = getFragmentManager().beginTransaction();
                                Vote v = new Vote();
                                v.setArguments(b);
                                fragmentTransation.replace(R.id.view_main, v);
                                fragmentTransation.commit();
//                                startActivity(i);
                            } else {
                                Toast.makeText(MainActivity.this, "Already Voted!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Toast.makeText(MainActivity.this, "No such user", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
                }
                Log.d("", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(MainActivity.this, "No such user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openResults(View v) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Results resultsFragment = new Results();
        fragmentTransaction.replace(R.id.view_main, resultsFragment);
        fragmentTransaction.commit();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}