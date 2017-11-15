package com.example.android.electioncomission;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Vote extends Fragment {

    String userId;
    String constituency;
    TextView candidateName;
    TextView location;
    TextView constituencyName;
    Spinner party;
    ImageView partyImage;
    Button b;
    public Vote() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_vote, container, false);

        candidateName = (TextView)v.findViewById(R.id.candidateName);
        constituencyName = (TextView)v.findViewById(R.id.constituency);
        partyImage = (ImageView)v.findViewById(R.id.imageView);
        b = (Button)v.findViewById(R.id.button2);
        location = (TextView)v.findViewById(R.id.location);

        Bundle extras = getArguments();
        userId = extras.getString("userid");
        constituency = extras.getString("constituency");
        constituencyName.setText(constituency);
        List<String> list=new ArrayList<String>();
        list.add("BJP");
        list.add("Congress");
        list.add("Others");
        party=(Spinner) v.findViewById(R.id.party);
        ArrayAdapter<String> adp= new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        party.setAdapter(adp);
        party.setOnItemSelectedListener(new MyOnItemSelectedListener());

        b.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                vote(v);
            }
        });
        return v;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                getActivity());
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            location.setText(locationAddress);
        }
    }

    public void vote(View v){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        myRef.child("constituency").child(constituency.toLowerCase()).child(userId).child("voted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean voted = dataSnapshot.getValue(Boolean.class);
                if(!voted){
                    myRef.child("party").child(party.getSelectedItem().toString()).child(constituency.toLowerCase()).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Integer votes = dataSnapshot.getValue(Integer.class);
                            myRef.child("party").child(party.getSelectedItem().toString())
                                    .child(constituency.toLowerCase()).child("votes").setValue(++votes);
                            myRef.child("constituency").child(constituency.toLowerCase()).child(userId).child("voted").setValue(true);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Toast.makeText(getActivity(), "No such user", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else{
                    Toast.makeText(getActivity(), "Voted!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getActivity(), "No such user", Toast.LENGTH_SHORT).show();
            }
        });
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Results resultsFragment = new Results();
        fragmentTransaction.replace(R.id.view_main, resultsFragment);
        fragmentTransaction.commit();
    }

    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            StorageReference pathReference = FirebaseStorage.getInstance().getReference().child(party.getSelectedItem().toString().toLowerCase()+".png");
            Glide.with(getActivity())
                    .using(new FirebaseImageLoader())
                    .load(pathReference)
                    .into(partyImage);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.child("party").child(party.getSelectedItem().toString()).child(constituency.toLowerCase()).child("candidatename").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String candiName = dataSnapshot.getValue(String.class);
                    candidateName.setText(candiName);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(getActivity(), "No such user", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }
}
