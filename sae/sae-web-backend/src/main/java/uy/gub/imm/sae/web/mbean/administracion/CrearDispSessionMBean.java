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


public class CrearDispSessionMBean extends SessionCleanerMBean implements RemovableFromSession {
	
	public static final String MSG_ID = "pantalla";
		
	@EJB(name="ejb/AgendaGeneralBean")
	private AgendaGeneral generalEJB;
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	private RowList<CupoPorDia> cuposPorDia;
	
	private RowList<DisponibilidadReserva> disponibilidadesDelDiaMatutina;
	private RowList<DisponibilidadReserva> disponibilidadesDelDiaVespertina;
	
	//Fecha para crear disponibilidades.
	private Date fechaCrear;
	private Date horaDesde;
	private Date horaHasta;
	private Integer frecuencia;
	private Integer cupo;
	private Integer horaD;
	private Integer minD;
	private Integer horaH;
	private Integer minH;
	

	@PostConstruct
	public void init() {
		if (generalEJB  == null) generalEJB  = (AgendaGeneral)lookupEJB(SAEProfile.getInstance().EJB_AGENDA_GENERAL_JNDI);
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
	}
	
	public RowList<CupoPorDia> getCuposPorDia() {
		return cuposPorDia;
	}
	public void setCuposPorDia(RowList<CupoPorDia> cuposPorDia) {
		this.cuposPorDia = cuposPorDia;
	}
	
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
	
	

	public Date getFechaCrear() {
		return fechaCrear;
	}
	public void setFechaCrear(Date fechaCrear) {
		this.fechaCrear = fechaCrear;
	}

	public Date getHoraDesde() {
		return horaDesde;
	}

	public void setHoraDesde(Date horaDesde) {
		this.horaDesde = horaDesde;
	}

	public Date getHoraHasta() {
		return horaHasta;
	}

	public void setHoraHasta(Date horaHasta) {
		this.horaHasta = horaHasta;
	}
	public Integer getFrecuencia() {
		return frecuencia;
	}
	public void setFrecuencia(Integer frecuencia) {
		this.frecuencia = frecuencia;
	}
	public Integer getCupo() {
		return cupo;
	}
	public void setCupo(Integer cupo) {
		this.cupo = cupo;
	}
	public Integer getHoraD() {
		return horaD;
	}
	public void setHoraD(Integer horaD) {
		this.horaD = horaD;
	}
	public Integer getMinD() {
		return minD;
	}
	public void setMinD(Integer minD) {
		this.minD = minD;
	}
	public Integer getHoraH() {
		return horaH;
	}
	public void setHoraH(Integer horaH) {
		this.horaH = horaH;
	}
	public Integer getMinH() {
		return minH;
	}
	public void setMinH(Integer minH) {
		this.minH = minH;
	}
	

}


