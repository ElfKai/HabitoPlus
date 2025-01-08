package com.example.habitoplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {
    private RecyclerView recyclerHabitoProgress;
    private HabitoProgressAdapter adapter;
    private List<Habito> habitosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recyclerHabitoProgress = findViewById(R.id.recycler_habito_progress);
        recyclerHabitoProgress.setLayoutManager(new LinearLayoutManager(this));
        ImageButton back = findViewById(R.id.regresar1);

        // Cargar hábitos desde Firestore
        cargarHabitos();

        back.setOnClickListener(v -> openMain());
    }

    public void sendEmail(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Compartir desde la app");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "¡Hola! Estoy compartiendo esto desde mi app.");
        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correo usando..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Profile.this, "No hay clientes de correo instalados.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarHabitos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("habitos")

                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Habito> habitos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Habito habito = document.toObject(Habito.class);
                            habito.setId(document.getId());  // Asigna el ID de Firestore
                            habitos.add(habito);
                        }
                        adapter = new HabitoProgressAdapter(habitos);
                        recyclerHabitoProgress.setAdapter(adapter);
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private void openMain() {
        Intent intent = new Intent(Profile.this, MainActivity.class);
        startActivity(intent);
    }

}
