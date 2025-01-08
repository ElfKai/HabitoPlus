package com.example.habitoplus;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HabitosAdapter extends RecyclerView.Adapter<HabitosAdapter.HabitoViewHolder> {

    private List<Habito> habitos;
    private OnHabitClickListener listener;

    // Constructor
    public HabitosAdapter(List<Habito> habitos) {
        this.habitos = habitos != null ? habitos : new ArrayList<>();  // Evitar NPE
    }

    // Interfaz para manejar clics
    public interface OnHabitClickListener {
        void onHabitClick(Habito habito);
    }

    // Método para configurar el listener
    public void setOnHabitClickListener(OnHabitClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habito, parent, false);
        return new HabitoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitoViewHolder holder, int position) {
        Habito habito = habitos.get(position);
        holder.tvNombre.setText(habito.getTitulo());
        holder.tvDescripcion.setText(habito.getDescripcion());
        holder.tvHora.setText(habito.getHora());

        // Formatear y mostrar las fechas de inicio y fin
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaInicio = habito.getFechaInicio() != null ? dateFormat.format(habito.getFechaInicio()) : "N/A";
        String fechaFin = habito.getFechaFin() != null ? dateFormat.format(habito.getFechaFin()) : "N/A";
        holder.tvFechas.setText(String.format("Inicio: %s | Fin: %s", fechaInicio, fechaFin));

        // Indicar si es un hábito repetitivo
        holder.tvRepetitivo.setVisibility(habito.isRepetirTodosLosDias() ? View.VISIBLE : View.GONE);

        // Verificar si el hábito está completado y ajustar la vista
        if (habito.isCompleted()) {
            holder.itemView.setAlpha(0.5f);  // Hacer el ítem más transparente
            holder.itemView.setClickable(false); // Deshabilitar el clic
            holder.tvNombre.setTextColor(Color.GRAY); // Cambiar color del texto
        } else {
            holder.itemView.setAlpha(1.0f);  // Restaurar la opacidad
            holder.itemView.setClickable(true); // Hacer el ítem clickeable
            holder.tvNombre.setTextColor(Color.BLACK); // Restaurar color del texto
        }

        // Manejar clic en el ítem solo si el hábito no está completado
        holder.itemView.setOnClickListener(v -> {
            if (!habito.isCompleted() && listener != null) {
                listener.onHabitClick(habito); // Notificar el clic
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitos.size();
    }

    static class HabitoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvHora, tvFechas, tvRepetitivo;

        public HabitoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_habito);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_habito);
            tvHora = itemView.findViewById(R.id.tv_hora_habito);
            tvFechas = itemView.findViewById(R.id.tv_fechas_habito);
            tvRepetitivo = itemView.findViewById(R.id.tv_repetitivo_habito);
        }
    }

    // Actualiza la lista de hábitos y notifica que los datos han cambiado
    public void updateData(List<Habito> nuevosHabitos) {
        this.habitos.clear();
        this.habitos.addAll(nuevosHabitos);
        notifyDataSetChanged();
    }

    // Método para notificar cuando solo se actualiza un ítem
    public void updateItem(int position) {
        notifyItemChanged(position);
    }
}
