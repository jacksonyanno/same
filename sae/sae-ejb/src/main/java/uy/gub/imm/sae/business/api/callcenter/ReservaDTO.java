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

package uy.gub.imm.sae.business.api.callcenter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author im2716295
 *
 */
public class ReservaDTO implements Serializable {
	
	private static final long serialVersionUID = -3515560740862668921L;
	
	private Integer id;
	private Date fechaYhora;
	private Map<String, String> datos;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getFechaYhora() {
		return fechaYhora;
	}
	public void setFechaYhora(Date fechaYhora) {
		this.fechaYhora = fechaYhora;
	}
	public Map<String, String> getDatos() {
		return datos;
	}
	public void setDatos(Map<String, String> datos) {
		this.datos = datos;
	}
}
