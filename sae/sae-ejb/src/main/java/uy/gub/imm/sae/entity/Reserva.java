/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2009  IMM - Intendencia Municipal de Montevideo
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.

 * SAME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uy.gub.imm.sae.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import uy.gub.imm.sae.common.enumerados.Estado;

@Entity
@Table (name = "ae_reservas")
public class Reserva implements Serializable {

	private static final long serialVersionUID = 3500715468120358550L;

	private Integer id;
	private Integer numero;
	private Estado estado;
	private String observaciones;
	private Date fechaCreacion;
	private Date fechaActualizacion;
	private Integer version;
	private Llamada llamada;
	private String origen;
	private String ucrea;
	private String ucancela;

	private List<Disponibilidad> disponibilidades;
	private Set<DatoReserva> datosReserva;
	// Se agrega para el reporte de Vino-No vino
	private List<Atencion> atenciones;
	
	public Reserva () {
		estado = Estado.P;
		fechaCreacion = new Date();
		fechaActualizacion = fechaCreacion;
		disponibilidades = new ArrayList<Disponibilidad>();
		datosReserva = new HashSet<DatoReserva>();
	}
	
	public Reserva (Integer id, Integer numero, Estado estado, String obs, Date creacion, Disponibilidad d, DatoReserva dr) {
	
		this.id = id;
		this.numero = numero;
		this.estado = estado;
		this.observaciones = obs;
		this.fechaCreacion = creacion;
		this.fechaActualizacion = creacion;
		
		this.disponibilidades = new ArrayList<Disponibilidad>();
		this.disponibilidades.add(d);
		this.datosReserva = new HashSet<DatoReserva>();
		this.datosReserva.add(dr);
	}


	@Id
	@GeneratedValue (strategy = GenerationType.SEQUENCE, generator="seq_reserva")
	@SequenceGenerator (name ="seq_reserva", initialValue = 1, sequenceName = "s_ae_reserva",allocationSize=1)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column ()
	public Integer getNumero() {
		return numero;
	}
	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	@Column (nullable = false, length=1)
	@Enumerated (EnumType.STRING)
	public Estado getEstado() {
		return estado;
	}
	public void setEstado(Estado estado) {
		this.estado = estado;
	}
	
	@Column (length = 100)
	public String getObservaciones() {
		return observaciones;
	}
	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
	
	@OneToMany (mappedBy = "reserva", fetch = FetchType.EAGER)
	public Set<DatoReserva> getDatosReserva() {
		return datosReserva;
	}
	public void setDatosReserva(Set<DatoReserva> datosReserva) {
		this.datosReserva = datosReserva;
	}
	
	// Se agrega la lista de atencion para poder hacer el reporte
	// de vino - No vino
	@XmlTransient
	@OneToMany (mappedBy="reserva")
	public List <Atencion> getAtenciones(){
		return atenciones;
	}
	
	public void setAtenciones(List<Atencion> atenciones){
		this.atenciones = atenciones;
	}
	
	@ManyToMany(fetch = FetchType.EAGER)
	//@ManyToMany (mappedBy = "reservas")
	@JoinTable(name = "ae_reservas_disponibilidades",
			   inverseJoinColumns={@JoinColumn (name = "aedi_id")},
			   joinColumns={@JoinColumn (name = "aers_id")}
	)
	public List<Disponibilidad> getDisponibilidades() {
		return disponibilidades;
	}
	public void setDisponibilidades(List<Disponibilidad> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}

	@Column (name = "fcrea", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	@Column (name = "fact", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}
	
	@XmlTransient
	@OneToOne(optional=true, mappedBy="reserva")
	public Llamada getLlamada() {
		return llamada;
	}

	public void setLlamada(Llamada llamada) {
		this.llamada = llamada;
	}

	@Version
	@Column(name="version", nullable=false)
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Transient
	public String getEstadoDescripcion(){
		
		return getEstadoDescripcion(estado);
		
	}
	
	public void setEstadoDescripcion(String estado){
		
	}
	
	@Transient
	public String getEstadoDescripcion(Estado e){
		
		String resultado = "";
		
		if (e != null){
			resultado = e.getDescripcion();
		}        
		return resultado;
	}

	@Override
	public String toString() {
		String strDisp = "disponibilidades=";
		for (Iterator<Disponibilidad> iterator = disponibilidades.iterator(); iterator.hasNext();) {
			Disponibilidad disp = iterator.next();
			strDisp+= disp.toString()+",";			
		}
		
		String strDatos = "datos=";
		
		for (Iterator<DatoReserva> iterator = datosReserva.iterator(); iterator.hasNext();) {
			DatoReserva dato = iterator.next();
			strDatos+=dato.toString()+",";
		}
		
		return "Reserva [id="+ id + "," + strDisp + "," + strDatos +"]";
	}

	@Column (name = "origen", length = 1)
	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}
	
	@Column (name = "ucrea", length = 30)
	public String getUcrea() {
		return ucrea;
	}

	public void setUcrea(String ucrea) {
		this.ucrea = ucrea;
	}

	public String getUcancela() {
		return ucancela;
	}

	public void setUcancela(String ucancela) {
		this.ucancela = ucancela;
	}
		
}
