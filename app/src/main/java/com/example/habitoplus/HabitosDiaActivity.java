package com.example.habitoplus;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HabitosDiaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitosAdapter habitosAdapter;
    private ProgressBar progressBar; // Barra de progreso para indicar carga
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habitos_dia);

        TextView tituloDia = findViewById(R.id.titulo_dia);
        recyclerView = findViewById(R.id.recycler_habitos);
        progressBar = findViewById(R.id.progress_bar); // ProgressBar desde el layout

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitosAdapter = new HabitosAdapter(new ArrayList<>());
        recyclerView.setAdapter(habitosAdapter);

        // Obtener Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el día del Intent
        String dia = getIntent().getStringExtra("dia");

        if (dia != null) {
            tituloDia.setText("Hábitos de " + dia);
            cargarHabitosDelDia(dia); // Cargar hábitos del día específico
        } else {
            Toast.makeText(this, "Error: Día no encontrado", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad si no hay día válido
        }
    }

    // Método para cargar los hábitos de un día específico
    private void cargarHabitosDelDia(String dia) {
        progressBar.setVisibility(View.VISIBLE); // Mostrar barra de progreso

        // Crear listas para los hábitos
        List<Habito> habitos = new ArrayList<>();

        // Obtener los hábitos que se repiten todos los días
        Task<QuerySnapshot> repetidosTask = db.collection("habitos")
                .whereEqualTo("repetirTodosLosDias", true)
                .get();

        // Obtener los hábitos específicos del día actual
        Task<QuerySnapshot> diaTask = db.collection("habitos")
                .whereEqualTo("dia", dia)
                .get();

        // Esperar a que ambas tareas terminen
        Tasks.whenAllSuccess(repetidosTask, diaTask)
                .addOnSuccessListener(tasks -> {
                    // Procesar los hábitos repetidos
                    QuerySnapshot queryDocumentSnapshotsRepetidos = repetidosTask.getResult();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshotsRepetidos) {
                        Habito habito = document.toObject(Habito.class);
                        habito.setId(document.getId()); // Establece el ID del documento
                        habitos.add(habito); // Añadir el hábito repetido
                    }

                    // Procesar los hábitos específicos del día
                    QuerySnapshot queryDocumentSnapshotsDia = diaTask.getResult();
                    for (QueryDocumentSnapshot documentDia : queryDocumentSnapshotsDia) {
                        Habito habitoDia = documentDia.toObject(Habito.class);
                        habitoDia.setId(documentDia.getId()); // Establece el ID del documento
                        habitos.add(habitoDia); // Añadir el hábito específico del día
                    }

                    // Actualizar el RecyclerView con la lista de hábitos combinados
                    habitosAdapter.updateData(habitos);
                    progressBar.setVisibility(View.GONE); // Ocultar barra de progreso
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE); // Ocultar barra de progreso
                    Log.w("Firestore", "Error al cargar los hábitos", e);
                    Toast.makeText(this, "Error al cargar los hábitos", Toast.LENGTH_SHORT).show();
                });
    }


}
