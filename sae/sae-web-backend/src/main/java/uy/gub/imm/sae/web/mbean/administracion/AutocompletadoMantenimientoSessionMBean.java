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

import javax.annotation.PostConstruct;

import uy.gub.imm.sae.entity.ServicioAutocompletar;
import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;

public class AutocompletadoMantenimientoSessionMBean extends SessionCleanerMBean implements RemovableFromSession {

	private Integer autocompletadosTablePageIndex;
	private Boolean modoCreacion;
	private Boolean modoEdicion;
	private ServicioAutocompletar autocompletado;

	private Integer parametrosTablePageIndex;
	
	
	@PostConstruct
	public void init() {
		modoCreacion = false;
		modoEdicion = false;
	}

	public Integer getAutocompletadosTablePageIndex() {
		return autocompletadosTablePageIndex;
	}

	public void setAutocompletadosTablePageIndex(Integer autocompletadosTablePageIndex) {
		this.autocompletadosTablePageIndex = autocompletadosTablePageIndex;
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

	public ServicioAutocompletar getAutocompletado() {
		return autocompletado;
	}

	public void setAutocompletado(ServicioAutocompletar autocompletado) {
		this.autocompletado = autocompletado;
	}

	public Integer getParametrosTablePageIndex() {
		return parametrosTablePageIndex;
	}

	public void setParametrosTablePageIndex(Integer parametrosTablePageIndex) {
		this.parametrosTablePageIndex = parametrosTablePageIndex;
	}

	
}
