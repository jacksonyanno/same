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

package uy.gub.imm.sae.web.mbean.administracion;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import uy.gub.imm.sae.business.api.AgendaGeneral;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.DisponibilidadReserva;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.web.common.CupoPorDia;
import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.RowList;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;


public class GenDispSessionMBean extends SessionCleanerMBean implements RemovableFromSession {
	
	public static final String MSG_ID = "pantalla";
		
	@EJB(name="ejb/AgendaGeneralBean")
	private AgendaGeneral generalEJB;
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	private CupoPorDia cupoPorDiaSeleccionado;
	private RowList<CupoPorDia> cuposPorDia;
	
	private DisponibilidadReserva dispSeleccionado;
	private Boolean mostrarDisponibilidad = true;
	
	//private List<Disponibilidad> disponibilidades;
	private RowList<DisponibilidadReserva> disponibilidadesDelDiaMatutina;
	private RowList<DisponibilidadReserva> disponibilidadesDelDiaVespertina;
	private RowList<DisponibilidadReserva> disponibilidadesDelDiaMatutinaModif;
	private RowList<DisponibilidadReserva> disponibilidadesDelDiaVespertinaModif;

	
	private Date fechaDesde;
	private Date fechaHasta;
	private Date fechaActual;
	//private Date fechaModifCupo;
	
	//Fecha para tomar como modelo para generar disponibilidades.
	private Date fechaModelo;
	private Date fechaInicial;
	private Date fechaFinal;

	
	
	private int pagCupo;
	//private int pagDisp;


	@PostConstruct
	public void init() {
		if (generalEJB  == null) generalEJB  = (AgendaGeneral)lookupEJB(SAEProfile.getInstance().EJB_AGENDA_GENERAL_JNDI);
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
	}
	public Date getFechaModelo() {
		return fechaModelo;
	}
	public void setFechaModelo(Date fechaModelo) {
		this.fechaModelo = fechaModelo;
	}

	
	public int getPagCupo() {
		return pagCupo;
	}
	public void setPagCupo(int pagCupo) {
		this.pagCupo = pagCupo;
	}
	public Date getFechaActual() {
		return fechaActual;
	}
	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}


	
	public CupoPorDia getCupoPorDiaSeleccionado() {
		return cupoPorDiaSeleccionado;
	}
	public void setCupoPorDiaSeleccionado(CupoPorDia cupoPorDiaSeleccionado) {
		this.cupoPorDiaSeleccionado = cupoPorDiaSeleccionado;
	}
	public RowList<CupoPorDia> getCuposPorDia() {
		return cuposPorDia;
	}
	public void setCuposPorDia(RowList<CupoPorDia> cuposPorDia) {
		this.cuposPorDia = cuposPorDia;
	}
	
	public Date getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
	/*
	public int getPagDisp() {
		return pagDisp;
	}
	public void setPagDisp(int pagDisp) {
		this.pagDisp = pagDisp;
	}

	public List<Disponibilidad> getDisponibilidades() {
		return disponibilidades;
	}

	public void setDisponibilidades(List<Disponibilidad> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}
	*/
	
	public RowList<DisponibilidadReserva> getDisponibilidadesDelDiaMatutina() {
		return disponibilidadesDelDiaMatutina;
	}
	public void setDisponibilidadesDelDiaMatutina(
			RowList<DisponibilidadReserva> disponibilidadesDelDiaMatutina) {
		this.disponibilidadesDelDiaMatutina = disponibilidadesDelDiaMatutina;
	}
	public RowList<DisponibilidadReserva> getDisponibilidadesDelDiaVespertina() {
		return disponibilidadesDelDiaVespertina;
	}
	public void setDisponibilidadesDelDiaVespertina(
			RowList<DisponibilidadReserva> disponibilidadesDelDiaVespertina) {
		this.disponibilidadesDelDiaVespertina = disponibilidadesDelDiaVespertina;
	}
	
	
	
	public DisponibilidadReserva getDispSeleccionado() {
		return dispSeleccionado;
	}
	public void setDispSeleccionado(DisponibilidadReserva dispSeleccionado) {
		this.dispSeleccionado = dispSeleccionado;
	}

	public Boolean getMostrarDisponibilidad() {

		if (this.getDispSeleccionado() != null ) {
			this.mostrarDisponibilidad = true;
		}
		else {
			this.mostrarDisponibilidad = false;
		}
		return this.mostrarDisponibilidad;
	}


	public void setMostrarDisponibilidad(Boolean mostrarDisponibilidad) {
		this.mostrarDisponibilidad = mostrarDisponibilidad;
	}
	
	public RowList<DisponibilidadReserva> getDisponibilidadesDelDiaMatutinaModif() {
		return disponibilidadesDelDiaMatutinaModif;
	}
	public void setDisponibilidadesDelDiaMatutinaModif(
			RowList<DisponibilidadReserva> disponibilidadesDelDiaMatutinaModif) {
		this.disponibilidadesDelDiaMatutinaModif = disponibilidadesDelDiaMatutinaModif;
	}
	public RowList<DisponibilidadReserva> getDisponibilidadesDelDiaVespertinaModif() {
		return disponibilidadesDelDiaVespertinaModif;
	}
	public void setDisponibilidadesDelDiaVespertinaModif(
			RowList<DisponibilidadReserva> disponibilidadesDelDiaVespertinaModif) {
		this.disponibilidadesDelDiaVespertinaModif = disponibilidadesDelDiaVespertinaModif;
	}
	public Date getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}
	public Date getFechaFinal() {
		return fechaFinal;
	}
	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}
	
	/*
	public Date getFechaModifCupo() {
		return fechaModifCupo;
	}
	public void setFechaModifCupo(Date fechaModifCupo) {
		this.fechaModifCupo = fechaModifCupo;
	}
	*/


}


