//package com.example.chatsapp;
//
//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import android.os.Bundle;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.chatsapp.Adapters.Top_Status_Adapter;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import java.util.ArrayList;
//
//public class StatusActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    private Top_Status_Adapter statusAdapter;
//    private ArrayList<Status> statuses;
//    private DatabaseReference databaseReference;
//    private FloatingActionButton fab;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_status);
//
//        recyclerView = findViewById(R.id.recyclerView);
//        fab = findViewById(R.id.fab);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        statuses = new ArrayList<>();
//        statusAdapter = new StatusAdapter(this, statuses);
//        recyclerView.setAdapter(statusAdapter);
//
//        databaseReference = FirebaseDatabase.getInstance().getReference("stories");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                statuses.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    String name = dataSnapshot.child("name").getValue(String.class);
//                    String profileImage = dataSnapshot.child("profileImage").getValue(String.class);
//                    String updatedTime = dataSnapshot.child("lastUpdated").getValue(String.class);
//                    statuses.add(new Status(name, profileImage, updatedTime));
//                }
//                statusAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(StatusActivity.this, "Failed to load statuses", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fab.setOnClickListener(view -> {
//            // Handle status upload
//        });
//    }
//}
