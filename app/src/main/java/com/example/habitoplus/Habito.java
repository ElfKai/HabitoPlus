package com.example.habitoplus;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Habito {
    private String id; // ID del documento Firestore
    private String titulo;
    private String descripcion;
    private String hora;
    private String dia; // Día específico, o vacío si se repite todos los días
    private int progreso;
    private boolean isCompleted;  // Indica si el hábito está completado
    private boolean repetirTodosLosDias; // Indica si el hábito se repite diariamente
    private Date fechaInicio; // Fecha de inicio del hábito
    private Date fechaFin; // Fecha de finalización del hábito

    // Constructor vacío requerido por Firestore
    public Habito() {
        // Necesario para Firestore
    }

    // Constructor completo con los nuevos campos
    public Habito(String titulo, String descripcion, String hora, String dia, boolean repetirTodosLosDias,
                  Date fechaInicio, Date fechaFin, int progreso) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.hora = hora;
        this.dia = dia;
        this.repetirTodosLosDias = repetirTodosLosDias;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.progreso = progreso;
        this.isCompleted = false; // Inicializado como no completado por defecto
    }

    // Getters y Setters
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isRepetirTodosLosDias() {
        return repetirTodosLosDias;
    }

    public void setRepetirTodosLosDias(boolean repetirTodosLosDias) {
        this.repetirTodosLosDias = repetirTodosLosDias;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }
}
