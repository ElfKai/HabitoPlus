package com.example.habitoplus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NuevoHabitoDialogFragment.OnNuevoHabitoListener{

    private TextView timerText, mostrardia;
    private ImageButton btnPause, menuIcon, darkModeToggle, btnProfile;
    private Button btnExplore, btnUpdates,btnNuevoHabito;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseFirestore db;
    private String diaActual;
    private RecyclerView recyclerView;
    private HabitosAdapter habitosAdapter;
    private List<Habito> habitosList = new ArrayList<>();
    private static final String CHANNEL_ID = "HabitoPlus";
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isBreakTime = false;
    private long timeLeftInMillis = 10 * 1000; // 25 minutos( 10 segundos por temas de ejemplo)
    private long breakTimeInMillis = 10 * 1000; // 5 minutos ( 10 segundos por temas de ejemplo)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        initializeViews();

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitosAdapter = new HabitosAdapter(habitosList);
        recyclerView.setAdapter(habitosAdapter);

        diaActual = DiaSemana();
        mostrardia.setText(diaActual);

        // Cargar hábitos del día
        cargarHabitosDelDia(diaActual);

        // Configurar listeners
        setListeners();

        btnNuevoHabito.setOnClickListener(v -> {
            // Crear y mostrar el DialogFragment
            NuevoHabitoDialogFragment nuevoHabitoDialog = new NuevoHabitoDialogFragment();
            nuevoHabitoDialog.show(getSupportFragmentManager(), "NuevoHabitoDialog");
        });
    }

    private void initializeViews() {
        timerText = findViewById(R.id.timer_text);
        btnPause = findViewById(R.id.btn_pause);
        menuIcon = findViewById(R.id.menu_icon);
        darkModeToggle = findViewById(R.id.dark_mode_toggle);
        btnExplore = findViewById(R.id.btn_explore);
        btnUpdates = findViewById(R.id.btn_updates);
        mostrardia = findViewById(R.id.dia_actual);
        recyclerView = findViewById(R.id.recycler_habitos);
        btnProfile = findViewById(R.id.btn_profile);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnNuevoHabito = findViewById(R.id.btnNuevoHabito);
        db = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        darkModeToggle.setOnClickListener(v -> toggleDarkMode());
        btnPause.setOnClickListener(v -> toggleTimer());
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));
        btnProfile.setOnClickListener(v -> openProfile());
        btnExplore.setOnClickListener(v -> showToast("Explorar clickeado"));
        btnUpdates.setOnClickListener(v -> showToast("Actualizaciones clickeado"));

        habitosAdapter.setOnHabitClickListener(habito -> iniciarTemporizador(habito));

        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item.getItemId());
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            pauseTimer();
        } else {
            startTimer(timeLeftInMillis);
        }
    }

    private void handleNavigationItemSelected(int id) {
        HashMap<Integer, String> dias = new HashMap<>();
        dias.put(R.id.lunes, "Lunes");
        dias.put(R.id.martes, "Martes");
        dias.put(R.id.miercoles, "Miércoles");
        dias.put(R.id.jueves, "Jueves");
        dias.put(R.id.viernes, "Viernes");
        dias.put(R.id.sabado, "Sábado");
        dias.put(R.id.domingo, "Domingo");

        String dia = dias.get(id);
        if (dia != null) {
            openHabitsForDay(dia);
        }
    }

    private void openHabitsForDay(String dia) {
        Intent intent = new Intent(this, HabitosDiaActivity.class);
        intent.putExtra("dia", dia);
        startActivity(intent);
    }

    private void openProfile() {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    private void startTimer(long timeInMillis) {
        timeLeftInMillis = timeInMillis;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                if (!isBreakTime) {
                    // Inicia el descanso cuando termine el trabajo
                    timeLeftInMillis = breakTimeInMillis;
                    isBreakTime = true;

                    startTimer(timeLeftInMillis);  // Llamada recursiva con el tiempo restante
                } else {
                    // Si ya estamos en descanso, reinicia el temporizador de trabajo
                    timeLeftInMillis = 10 * 1000;  // 10 segundos de trabajo( Tiempo ejemplo, realmente deberian de ser 25min)
                    showNotification("¡Habito Completado!", "Felicidades has completado un habito.");
                    isBreakTime = false;
                    updateTimerText();
                }
            }
        };
        countDownTimer.start();
        isTimerRunning = true;
        btnPause.setImageResource(R.drawable.ic_play_foreground);
    }

    private void iniciarTemporizador(Habito habito) {
        // Configurar el temporizador para iniciar desde 25 minutos
        timeLeftInMillis = 10 * 1000; // 25 minutos(10 segundos por el ejemplo)
        isBreakTime = false;

        // Iniciar el temporizador
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                // Cuando el tiempo de trabajo termine, iniciar el tiempo de descanso
                isBreakTime = true;
                timeLeftInMillis = breakTimeInMillis;
                startTimer(timeLeftInMillis);
                habito.setCompleted(true);// Marca el hábito como completado
                actualizarProgresoHabito(habito);
                habitosAdapter.notifyDataSetChanged();  // Notificar al adaptador que el hábito fue completado
                showNotification("¡Descanso!", "Es hora de tomar un descanso.");
            }
        };
        countDownTimer.start();
        isTimerRunning = true;
        btnPause.setImageResource(R.drawable.ic_play_foreground);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        btnPause.setImageResource(R.drawable.ic_play_foreground);
    }

    private void resetTimer(long duration, boolean isBreak) {
        timeLeftInMillis = duration;
        isBreakTime = isBreak;
        updateTimerText();
        if (isBreak) startTimer(duration);
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void cargarHabitosDelDia(String dia) {
        // Limpiar la lista antes de agregar nuevos hábitos
        habitosList.clear();

        // Obtener los hábitos que se repiten todos los días
        db.collection("habitos")
                .whereEqualTo("repetirTodosLosDias", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Procesar los hábitos repetidos
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Habito habito = document.toObject(Habito.class);
                        habito.setId(document.getId()); // Establecer el ID del documento
                        habitosList.add(habito); // Añadir el hábito repetido
                    }

                    // Obtener los hábitos específicos del día actual
                    db.collection("habitos")
                            .whereEqualTo("dia", dia)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshotsDia -> {
                                // Procesar los hábitos específicos del día
                                for (QueryDocumentSnapshot documentDia : queryDocumentSnapshotsDia) {
                                    Habito habitoDia = documentDia.toObject(Habito.class);
                                    habitoDia.setId(documentDia.getId()); // Establecer el ID del documento
                                    habitosList.add(habitoDia); // Añadir el hábito específico del día
                                }

                                // Actualizar el RecyclerView con la lista de hábitos combinados
                                habitosAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error al cargar los hábitos del día", e);
                                Toast.makeText(this, "Error al cargar los hábitos del día", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error al cargar los hábitos repetidos", e);
                    Toast.makeText(this, "Error al cargar los hábitos repetidos", Toast.LENGTH_SHORT).show();
                });
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public String DiaSemana() {
        Calendar calendar = Calendar.getInstance();
        String[] daysOfWeek = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        return daysOfWeek[(calendar.get(Calendar.DAY_OF_WEEK) - 1) % 7];
    }

    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences("user_preferences", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        prefs.edit().putBoolean("dark_mode", !isDarkMode).apply();
        AppCompatDelegate.setDefaultNightMode(isDarkMode
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void applyTheme() {
        boolean isDarkMode = getSharedPreferences("user_preferences", MODE_PRIVATE).getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onNuevoHabitoGuardado(Habito habito) {
        // Agregar el nuevo hábito a la lista de hábitos
        if (habito.getDia().equals(diaActual)) {  // Verificamos si el hábito corresponde al día actual
            habitosList.add(habito);
            habitosAdapter.notifyItemInserted(habitosList.size() - 1); // Notificar al adaptador que se ha agregado un nuevo ítem
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear el canal de notificación para API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal de Hábitos",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para el seguimiento de hábitos");
            notificationManager.createNotificationChannel(channel);
        }

        // Crear la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_explore_foreground) // Icono de la notificación
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Desaparece al tocarla

        // Mostrar la notificación
        notificationManager.notify(1, notificationBuilder.build());
    }

    private void actualizarProgresoHabito(Habito habito) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int nuevoProgreso = habito.getProgreso() + 10;
        if (nuevoProgreso > 100) {
            nuevoProgreso = 100;  // Limita el progreso a 100%
        }
        habito.setProgreso(nuevoProgreso); // Actualiza el progreso del hábito

        db.collection("habitos").document(habito.getId())
                .set(habito)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Progreso actualizado");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error al actualizar progreso", e);
                });
    }
}
