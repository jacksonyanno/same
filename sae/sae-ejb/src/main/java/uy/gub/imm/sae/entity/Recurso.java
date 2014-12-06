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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table (name = "ae_recursos")
public class Recurso implements Serializable {
	//@  Inheritance(strategy=InheritanceType.JOINED)

	private static final long serialVersionUID = -5197426783029830293L;

	private Integer id;
	private String nombre;
	private String descripcion;
	private Date fechaInicio;
	private Date fechaFin;
	private Date fechaInicioDisp;
	private Date fechaFinDisp;
	private Integer diasInicioVentanaIntranet;
	private Integer diasVentanaIntranet;
	private Integer diasInicioVentanaInternet;
	private Integer diasVentanaInternet;
	private Integer ventanaCuposMinimos;
	private Integer cantDiasAGenerar;
	private Integer largoListaEspera;
	private Boolean reservaMultiple;
	private Integer version;
	private Date fechaBaja;
	private Boolean mostrarNumeroEnLlamador;
	private Boolean visibleInternet;
	private Boolean mostrarNumeroEnTicket;
	private Boolean usarLlamador;
	private String serie;
	private Boolean sabadoEsHabil;
	
	private Agenda agenda;
	private List<Plantilla> plantillas;
	private List<AgrupacionDato> agrupacionDatos;
	private List<Disponibilidad> disponibilidades;
	private List<DatoDelRecurso> datoDelRecurso;
	private List<DatoASolicitar> datosASolicitar;
	private List<ValidacionPorRecurso> validacionesPorRecurso;
	private List<AccionPorRecurso> accionesPorRecurso;
	private List<ServicioPorRecurso> autocompletadosPorRecurso;
	private TextoRecurso textoRecurso;
	
	public Recurso () {
		visibleInternet = false;
		plantillas = new ArrayList<Plantilla>();
		agrupacionDatos = new ArrayList<AgrupacionDato>();
		disponibilidades = new ArrayList<Disponibilidad>();
		datoDelRecurso = new ArrayList<DatoDelRecurso>();
		datosASolicitar = new ArrayList<DatoASolicitar>();
		validacionesPorRecurso = new ArrayList<ValidacionPorRecurso>();	
		accionesPorRecurso = new ArrayList<AccionPorRecurso>();
		
		fechaInicio = new Date();
		usarLlamador = true;
		sabadoEsHabil = false;
	}
	
	/**
	 * Constructor por copia no en profundidad.
	 */
	public Recurso (Recurso r) {
		plantillas = new ArrayList<Plantilla>();
		agrupacionDatos = new ArrayList<AgrupacionDato>();
		disponibilidades = new ArrayList<Disponibilidad>();
		datoDelRecurso = new ArrayList<DatoDelRecurso>();
		datosASolicitar = new ArrayList<DatoASolicitar>();
		validacionesPorRecurso = new ArrayList<ValidacionPorRecurso>();	
		accionesPorRecurso = new ArrayList<AccionPorRecurso>();
		
		id = r.getId();
		nombre = r.getNombre();
		descripcion = r.getDescripcion();
		fechaInicio = r.getFechaInicio();
		fechaFin = r.getFechaFin();
		fechaInicioDisp = r.getFechaInicioDisp();
		fechaFinDisp = r.getFechaFinDisp();
		diasInicioVentanaIntranet = r.getDiasInicioVentanaIntranet();
		diasVentanaIntranet = r.getDiasVentanaIntranet();
		diasInicioVentanaInternet = r.getDiasInicioVentanaInternet();
		diasVentanaInternet = r.getDiasVentanaInternet();
		ventanaCuposMinimos = r.getVentanaCuposMinimos();
		cantDiasAGenerar = r.getCantDiasAGenerar();
		largoListaEspera = r.getLargoListaEspera();
		reservaMultiple = r.getReservaMultiple();
		fechaBaja = r.getFechaBaja();
		mostrarNumeroEnLlamador = r.getMostrarNumeroEnLlamador();
		mostrarNumeroEnTicket = r.getMostrarNumeroEnTicket();
		usarLlamador = r.getUsarLlamador();
		serie = r.getSerie();
		visibleInternet = r.getVisibleInternet();
		sabadoEsHabil = r.getSabadoEsHabil();
		
		agenda = null; //no es copia en profundidad por tanto solo copio atributos simples. new Agenda(r.getAgenda());
	}

	
	@Id
	@GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "seq_recurso")
	@SequenceGenerator (name ="seq_recurso", initialValue = 1, sequenceName = "s_ae_recurso",allocationSize=1)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	@Column (nullable = false, length = 50)
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Column (nullable = false, length = 50)
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	
	@Column (name = "fecha_inicio", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaInicio() {
		return fechaInicio;
	}
	
	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
	@Column (name = "fecha_fin")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	@Column (name = "fecha_inicio_disp", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaInicioDisp() {
		return fechaInicioDisp;
	}
	public void setFechaInicioDisp(Date fechaInicioDisp) {
		this.fechaInicioDisp = fechaInicioDisp;
	}

	@Column (name = "fecha_fin_disp")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaFinDisp() {
		return fechaFinDisp;
	}
	public void setFechaFinDisp(Date fechaFinDisp) {
		this.fechaFinDisp = fechaFinDisp;
	}
	
	@Column (name="dias_inicio_ventana_intranet", nullable = false)
	public Integer getDiasInicioVentanaIntranet() {
		return diasInicioVentanaIntranet;
	}
	public void setDiasInicioVentanaIntranet(Integer diasInicioVentanaIntranet) {
		this.diasInicioVentanaIntranet = diasInicioVentanaIntranet;
	}

	@Column (name="dias_ventana_intranet", nullable = false)
	public Integer getDiasVentanaIntranet() {
		return diasVentanaIntranet;
	}
	public void setDiasVentanaIntranet(Integer diasVentanaIntranet) {
		this.diasVentanaIntranet = diasVentanaIntranet;
	}

	@Column (name="dias_inicio_ventana_internet", nullable = false)
	public Integer getDiasInicioVentanaInternet() {
		return diasInicioVentanaInternet;
	}
	public void setDiasInicioVentanaInternet(Integer diasInicioVentanaInternet) {
		this.diasInicioVentanaInternet = diasInicioVentanaInternet;
	}
	
	@Column (name="dias_ventana_internet", nullable = false)
	public Integer getDiasVentanaInternet() {
		return diasVentanaInternet;
	}

	public void setDiasVentanaInternet(Integer diasVentanaInternet) {
		this.diasVentanaInternet = diasVentanaInternet;
	}

	@Column (name="ventana_cupos_minimos", nullable = false)
	public Integer getVentanaCuposMinimos() {
		return ventanaCuposMinimos;
	}
	public void setVentanaCuposMinimos(Integer ventanaCuposMinimos) {
		this.ventanaCuposMinimos = ventanaCuposMinimos;
	}
	@Column (name="cant_dias_a_generar", nullable = false)
	public Integer getCantDiasAGenerar() {
		return cantDiasAGenerar;
	}
	public void setCantDiasAGenerar(Integer cantDiasAGenerar) {
		this.cantDiasAGenerar = cantDiasAGenerar;
	}
	@Column (name="largo_lista_espera", nullable = true)
	public Integer getLargoListaEspera() {
		return largoListaEspera;
	}
	public void setLargoListaEspera(Integer largoListaEspera) {
		this.largoListaEspera = largoListaEspera;
	}
	@Column (name="reserva_multiple", nullable = false)
	public Boolean getReservaMultiple() {
		return reservaMultiple;
	}
	public void setReservaMultiple(Boolean reservaMultiple) {
		this.reservaMultiple = reservaMultiple;
	}

	@Column (name = "fecha_baja")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaBaja() {
		return fechaBaja;
	}
	public void setFechaBaja(Date fechaBaja) {
		this.fechaBaja = fechaBaja;
	}

	@Column (name = "mostrar_numero_en_llamador", nullable = false)
	public Boolean getMostrarNumeroEnLlamador() {
		return mostrarNumeroEnLlamador;
	}
	public void setMostrarNumeroEnLlamador(Boolean mostrarNumeroEnLlamador) {
		this.mostrarNumeroEnLlamador = mostrarNumeroEnLlamador;
	}
	
	@Column (name = "mostrar_numero_en_ticket", nullable = false)
	public Boolean getMostrarNumeroEnTicket() {
		return mostrarNumeroEnTicket;
	}
	public void setMostrarNumeroEnTicket(Boolean mostrarNumeroEnTicket) {
		this.mostrarNumeroEnTicket = mostrarNumeroEnTicket;
	}
	
	@Column (name = "usar_llamador", nullable = false)
	public Boolean getUsarLlamador() {
		return usarLlamador;
	}
	
	public void setUsarLlamador(Boolean usarLlamador) {
		this.usarLlamador = usarLlamador;
	}

	@Column (name = "serie", nullable = true)
	public String getSerie() {
		return serie;
	}
	
	public void setSerie(String serie) {
		this.serie = serie;
	}
	
	@Column (name="visible_internet", nullable = false)
	public Boolean getVisibleInternet() {
		return visibleInternet;
	}
	public void setVisibleInternet(Boolean visibleInternet) {
		this.visibleInternet = visibleInternet;
	}
	
	@Column (name = "SABADO_ES_HABIL", nullable = false)
	public Boolean getSabadoEsHabil() {
		return sabadoEsHabil;
	}
	public void setSabadoEsHabil(Boolean sabadoEsHabil) {
		this.sabadoEsHabil = sabadoEsHabil;
	}
	@XmlTransient
	@ManyToOne (optional = false)
	@JoinColumn (name = "aeag_id", nullable = false)
	public Agenda getAgenda() {
		return agenda;
	}
	public void setAgenda(Agenda agenda) {
		this.agenda = agenda;
	}
	
	@XmlTransient
	@OneToMany (mappedBy = "recurso")
	public List<Plantilla> getPlantillas() {
		return plantillas;
	}
	public void setPlantillas(List<Plantilla> plantillas) {
		this.plantillas = plantillas;
	}
	
	@OneToMany (mappedBy = "recurso")
	public List<AgrupacionDato> getAgrupacionDatos() {
		return agrupacionDatos;
	}
	public void setAgrupacionDatos(List<AgrupacionDato> agrupacionDatos) {
		this.agrupacionDatos = agrupacionDatos;
	}

	@OneToMany (mappedBy = "recurso")
	public List<DatoDelRecurso> getDatoDelRecurso() {
		return datoDelRecurso;
	}
	public void setDatoDelRecurso(List<DatoDelRecurso> datoDelRecurso) {
		this.datoDelRecurso = datoDelRecurso;
	}
	
	@XmlTransient
	@OneToMany (mappedBy = "recurso")
	public List<Disponibilidad> getDisponibilidades() {
		return disponibilidades;
	}
	public void setDisponibilidades(List<Disponibilidad> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}
	@XmlTransient
	@OneToMany (mappedBy = "recurso")
	public List<DatoASolicitar> getDatoASolicitar() {
		return datosASolicitar;
	}
	public void setDatoASolicitar(List<DatoASolicitar> datosASolicitar) {
		this.datosASolicitar = datosASolicitar;
	}

	@XmlTransient
	@OneToMany (mappedBy = "recurso")
	public List<ValidacionPorRecurso> getValidacionesPorRecurso() {
		return validacionesPorRecurso;
	}

	public void setValidacionesPorRecurso(
			List<ValidacionPorRecurso> validacionesPorRecurso) {
		this.validacionesPorRecurso = validacionesPorRecurso;
	}
	
	@XmlTransient
	@OneToMany (mappedBy = "recurso")
	public List<AccionPorRecurso> getAccionesPorRecurso() {
		return accionesPorRecurso;
	}

	public void setAccionesPorRecurso(
			List<AccionPorRecurso> accionesPorRecurso) {
		this.accionesPorRecurso = accionesPorRecurso;
	}

	@OneToOne (mappedBy = "recurso", fetch = FetchType.EAGER, cascade={CascadeType.ALL})
	public TextoRecurso getTextoRecurso() {
		return textoRecurso;
	}

	public void setTextoRecurso(TextoRecurso textoRecurso) {
		this.textoRecurso = textoRecurso;
	}

	@Version
	@Column (name = "version", nullable = false)
	public Integer getVersion() {
		return version;
	}

	protected void setVersion(Integer version) {
		this.version = version;
	}

	@XmlTransient
	@OneToMany (mappedBy = "recurso")
	public List<ServicioPorRecurso> getAutocompletadosPorRecurso() {
		return autocompletadosPorRecurso;
	}
	
	public void setAutocompletadosPorRecurso(
			List<ServicioPorRecurso> autocompletadosPorRecurso) {
		this.autocompletadosPorRecurso = autocompletadosPorRecurso;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agenda == null) ? 0 : agenda.hashCode());
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Recurso other = (Recurso) obj;
		if (agenda == null) {
			if (other.agenda != null)
				return false;
		} else if (!agenda.equals(other.agenda))
			return false;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}

}
