package cl.duoc.wim100.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// representa la tabla candidatas en mysql
@Entity
@Table(name = "candidatas")
public class Candidata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    // no se pueden repetir correos
    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 80)
    private String cargo;

    @Column(length = 40)
    private String experiencia;

    private Double puntaje = 0.0;

    // Pendiente, En evaluación, Evaluada, Seleccionada, No seleccionada
    @Column(nullable = false, length = 20)
    private String estado = "Pendiente";

    @Column(length = 300)
    private String observacion = "";

    public Candidata() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getExperiencia() { return experiencia; }
    public void setExperiencia(String experiencia) { this.experiencia = experiencia; }

    public Double getPuntaje() { return puntaje; }
    public void setPuntaje(Double puntaje) { this.puntaje = puntaje; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
