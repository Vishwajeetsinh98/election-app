package com.example.android.electioncomission;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Results extends Fragment {
    TextView a1,a2,a3,d1,d2,d3,c1,c2,c3,total,nota;
    public Results() {
        // Required empty public constructor
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_results, container, false);
        a1 = (TextView) v.findViewById(R.id.a1);
        a2 = (TextView) v.findViewById(R.id.a2);
        a3 = (TextView) v.findViewById(R.id.a3);
        d1 = (TextView) v.findViewById(R.id.d1);
        d2 = (TextView) v.findViewById(R.id.d2);
        d3 = (TextView) v.findViewById(R.id.d3);
        c1 = (TextView) v.findViewById(R.id.c1);
        c2 = (TextView) v.findViewById(R.id.c2);
        c3 = (TextView) v.findViewById(R.id.c3);
        total = (TextView)v.findViewById(R.id.total);
        nota = (TextView)v.findViewById(R.id.nota);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        myRef.child("party").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<PartyData> partyVotes = new ArrayList<PartyData>();
                for(DataSnapshot party : dataSnapshot.getChildren()){
                    for(DataSnapshot city : party.getChildren()){

                        PartyData p = new PartyData(city.child("candidatename").getValue().toString(), Integer.parseInt(city.child("votes").getValue().toString()));
                        partyVotes.add(p);
                    }
                }
                a1.setText("BJP: " + partyVotes.get(0).votes);
                a2.setText("Congress: " + partyVotes.get(1).votes);
                a3.setText("Others: " + partyVotes.get(2).votes);
                d1.setText("BJP: " + partyVotes.get(3).votes);
                d2.setText("Congress: " + partyVotes.get(4).votes);
                d3.setText("Others: " + partyVotes.get(5).votes);
                Collections.sort(partyVotes, new Comparator<PartyData>(){
                    public int compare(PartyData s1, PartyData s2) {
                        return s2.votes - s1.votes;
                    }
                });
                c1.setText(partyVotes.get(0).candidateName + " " + partyVotes.get(0).votes);
                c2.setText(partyVotes.get(1).candidateName + " " + partyVotes.get(1).votes);
                c3.setText(partyVotes.get(2).candidateName + " " + partyVotes.get(2).votes);
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference();
                myRef.child("constituency").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<User> users = new ArrayList<User>();
                        int c = 0;
                        int voted = 0;
                        for(DataSnapshot city : dataSnapshot.getChildren()){
                            for(DataSnapshot user: city.getChildren()){
                                User u = new User(user.child("name").getValue().toString(), Boolean.parseBoolean(user.child("voted").getValue().toString()));
                                users.add(u);
                                c++;
                                if(u.voted)
                                    voted++;
                            }
                        }
                        double percentageVoted = voted * 100/ c;
                        double percentageNOTA = (c - voted) * 100 / c;
                        total.setText("Voted % " + percentageVoted);
                        nota.setText("NOTA % " + percentageNOTA);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Toast.makeText(getActivity(), "No such user", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getActivity(), "No such user", Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    private class PartyData{
        String candidateName;
        int votes;
        PartyData(String s, int v){
            candidateName = s;
            votes = v;
        }

    }

    private class User{
        String name;
        boolean voted;
        User(String n, boolean v){
            name = n;
            voted = v;
        }
    }
}
