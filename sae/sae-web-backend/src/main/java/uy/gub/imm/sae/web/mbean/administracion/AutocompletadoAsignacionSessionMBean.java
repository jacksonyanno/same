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

import java.util.List;

import javax.annotation.PostConstruct;

import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ParametrosAutocompletar;
import uy.gub.imm.sae.entity.ServicioAutocompletar;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;

public class AutocompletadoAsignacionSessionMBean extends SessionCleanerMBean implements RemovableFromSession {

	private Boolean modoCreacion;
	private Boolean modoEdicion;
	
	private Integer autocompletadosDelRecursoTablePageIndex;
	private Integer autocompletadosPorDatoTablePageIndex;

 	private List<ServicioAutocompletar> autocompletados;
 	
	private ServicioPorRecurso autocompletadoDelRecurso;
	private List<DatoASolicitar> datosASolicitarDelRecurso;
	private List<DatoASolicitar> datosASolicitarDelRecursoCopia;
	private List<String> nombresParametrosAutocompletar;
	private List<ParametrosAutocompletar> parametrosAutocompletar;

	@PostConstruct
	public void init() {
		modoCreacion = false;
		modoEdicion = false;
		
		autocompletadosDelRecursoTablePageIndex = 1;
		autocompletadosPorDatoTablePageIndex = 1;
		
	}
	
	public Integer getAutocompletadosDelRecursoTablePageIndex() {
		return autocompletadosDelRecursoTablePageIndex;
	}

	public void setAutocompletadosDelRecursoTablePageIndex(
			Integer autocompletadosDelRecursoTablePageIndex) {
		this.autocompletadosDelRecursoTablePageIndex = autocompletadosDelRecursoTablePageIndex;
	}

	public Integer getAutocompletadosPorDatoTablePageIndex() {
		return autocompletadosPorDatoTablePageIndex;
	}

	public void setAutocompletadosPorDatoTablePageIndex(
			Integer autocompletadosPorDatoTablePageIndex) {
		this.autocompletadosPorDatoTablePageIndex = autocompletadosPorDatoTablePageIndex;
	}

	public Boolean getModoCreacion() {
		return modoCreacion;
	}

	public void setModoCreacion(Boolean modoCreacion) {
		this.modoCreacion = modoCreacion;
	}

	public Boolean getModoEdicion() {
		return modoEdicion;
	}

	public void setModoEdicion(Boolean modoEdicion) {
		this.modoEdicion = modoEdicion;
	}

	public List<DatoASolicitar> getDatosASolicitarDelRecurso() {
		return datosASolicitarDelRecurso;
	}

	public void setDatosASolicitarDelRecurso(
			List<DatoASolicitar> datosASolicitarDelRecurso) {
		this.datosASolicitarDelRecurso = datosASolicitarDelRecurso;
	}
	
	public List<DatoASolicitar> getDatosASolicitarDelRecursoCopia() {
		return datosASolicitarDelRecursoCopia;
	}

	public void setDatosASolicitarDelRecursoCopia(
			List<DatoASolicitar> datosASolicitarDelRecursoCopia) {
		this.datosASolicitarDelRecursoCopia = datosASolicitarDelRecursoCopia;
	}

	public List<String> getNombresParametrosAutocompletar() {
		return nombresParametrosAutocompletar;
	}

	public void setNombresParametrosAutocompletar(
			List<String> nombresParametrosAutocompletar) {
		this.nombresParametrosAutocompletar = nombresParametrosAutocompletar;
	}

	public List<ServicioAutocompletar> getAutocompletados() {
		return autocompletados;
	}

	public void setAutocompletados(List<ServicioAutocompletar> autocompletados) {
		this.autocompletados = autocompletados;
	}

	public ServicioPorRecurso getAutocompletadoDelRecurso() {
		return autocompletadoDelRecurso;
	}

	public void setAutocompletadoDelRecurso(
			ServicioPorRecurso autocompletadoDelRecurso) {
		this.autocompletadoDelRecurso = autocompletadoDelRecurso;
	}

	public List<ParametrosAutocompletar> getParametrosAutocompletar() {
		return parametrosAutocompletar;
	}

	public void setParametrosAutocompletar(
			List<ParametrosAutocompletar> parametrosAutocompletar) {
		this.parametrosAutocompletar = parametrosAutocompletar;
	}
	
}
