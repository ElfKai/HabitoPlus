package com.example.habitoplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HabitoProgressAdapter extends RecyclerView.Adapter<HabitoProgressAdapter.HabitoProgressViewHolder> {

    private List<Habito> habitos;

    // Constructor que asegura evitar listas nulas
    public HabitoProgressAdapter(List<Habito> habitos) {
        this.habitos = habitos != null ? habitos : new ArrayList<>();
    }

    @NonNull
    @Override
    public HabitoProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habito_progress, parent, false);
        return new HabitoProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitoProgressViewHolder holder, int position) {
        Habito habito = habitos.get(position);

        // Validar datos antes de usarlos
        if (habito != null) {
            holder.tvHabitName.setText(habito.getTitulo() != null ? habito.getTitulo() : "Hábito sin título");

            // Asegurarse de que el progreso esté en el rango [0, 100]
            int progreso = Math.max(0, Math.min(habito.getProgreso(), 100));
            holder.progressBar.setProgress(progreso);
        }
    }

    @Override
    public int getItemCount() {
        return habitos.size();
    }

    // Método para actualizar dinámicamente la lista de hábitos
    public void updateData(List<Habito> nuevosHabitos) {
        this.habitos.clear();
        if (nuevosHabitos != null) {
            this.habitos.addAll(nuevosHabitos);
        }
        notifyDataSetChanged();
    }

    // ViewHolder interno
    public static class HabitoProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName;
        ProgressBar progressBar;

        public HabitoProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.logro);
            progressBar = itemView.findViewById(R.id.progress_habit);
        }
    }
}
