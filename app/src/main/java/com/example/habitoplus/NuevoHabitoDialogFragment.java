package com.example.habitoplus;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NuevoHabitoDialogFragment extends DialogFragment {

    private EditText edtTitulo, edtDescripcion, edtHora;
    private Spinner spinnerDia;
    private CheckBox cbRepetirTodosLosDias;
    private DatePicker datePickerInicio, datePickerFin;
    private Button btnGuardar;
    private OnNuevoHabitoListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nuevo_habito, null);

        // Initialize views
        edtTitulo = view.findViewById(R.id.edtTitulo);
        edtDescripcion = view.findViewById(R.id.edtDescripcion);
        edtHora = view.findViewById(R.id.edtHora);
        spinnerDia = view.findViewById(R.id.spinnerDia);
        cbRepetirTodosLosDias = view.findViewById(R.id.cbRepetirTodosLosDias);
        datePickerInicio = view.findViewById(R.id.datePickerInicio);
        datePickerFin = view.findViewById(R.id.datePickerFin);
        btnGuardar = view.findViewById(R.id.btnGuardarHabito);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getDiasDeLaSemana());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapter);


        btnGuardar.setOnClickListener(v -> guardarHabito());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void guardarHabito() {
        String titulo = edtTitulo.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();
        String hora = edtHora.getText().toString().trim();
        String dia = spinnerDia.getSelectedItem().toString();  // Obtener el día seleccionado del Spinner
        boolean repetirTodosLosDias = cbRepetirTodosLosDias.isChecked();

        // Obtener las fechas de inicio y finalización
        Calendar fechaInicio = Calendar.getInstance();
        fechaInicio.set(datePickerInicio.getYear(), datePickerInicio.getMonth(), datePickerInicio.getDayOfMonth());

        Calendar fechaFin = Calendar.getInstance();
        fechaFin.set(datePickerFin.getYear(), datePickerFin.getMonth(), datePickerFin.getDayOfMonth());

        // Verificar que los campos no estén vacíos
        if (titulo.isEmpty() || descripcion.isEmpty() || hora.isEmpty()) {
            Toast.makeText(getActivity(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que la fecha de finalización sea posterior a la de inicio
        if (!fechaFin.after(fechaInicio)) {
            Toast.makeText(getActivity(), "La fecha de finalización debe ser posterior a la fecha de inicio.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamar al método para guardar el hábito en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Habito nuevoHabito = new Habito(titulo, descripcion, hora, dia, repetirTodosLosDias, fechaInicio.getTime(), fechaFin.getTime(), 0);

        db.collection("habitos")
                .add(nuevoHabito)
                .addOnSuccessListener(documentReference -> {
                    nuevoHabito.setId(documentReference.getId()); // Guarda el ID del documento
                    Log.d("Firestore", "Hábito añadido con ID: " + documentReference.getId());
                    if (mListener != null) {
                        mListener.onNuevoHabitoGuardado(nuevoHabito);  // Notificar que el hábito ha sido guardado
                    }
                    dismiss();  // Cerrar el diálogo
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error al guardar hábito", e));
    }

    // Método para obtener los días de la semana
    private List<String> getDiasDeLaSemana() {
        List<String> dias = new ArrayList<>();
        dias.add("Lunes");
        dias.add("Martes");
        dias.add("Miércoles");
        dias.add("Jueves");
        dias.add("Viernes");
        dias.add("Sábado");
        dias.add("Domingo");
        return dias;
    }

    public interface OnNuevoHabitoListener {
        void onNuevoHabitoGuardado(Habito habito);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNuevoHabitoListener) {
            mListener = (OnNuevoHabitoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNuevoHabitoListener");
        }
    }
}
