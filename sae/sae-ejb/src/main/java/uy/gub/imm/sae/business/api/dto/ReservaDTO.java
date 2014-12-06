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

package uy.gub.imm.sae.business.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReservaDTO implements Serializable{
        
		/**
	 * 
	 */
		private static final long serialVersionUID = -5249355111057790773L;
		private Integer numero;
		private Integer id;
		private String estado;
		private Date fecha;
		private Date horaInicio;
		private Map<String, Object> datos;
		private Integer puestoLlamada;
		private String origen;
		private String ucrea;
		private String ucancela;
		private String asistio;		

		public ReservaDTO() {
			datos = new HashMap<String, Object>();
		}
		public Map<String, Object> getDatos() {
			return datos;
		}
		public void setDatos(Map<String, Object> datos) {
			this.datos = datos;
		}
		public Integer getNumero() {
			return numero;
		}
		public void setNumero(Integer numero) {
			this.numero = numero;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getEstado() {
			return estado;
		}
		public void setEstado(String estado) {
			this.estado = estado;
		}
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public Date getHoraInicio() {
			return horaInicio;
		}
		public void setHoraInicio(Date horaInicio) {
			this.horaInicio = horaInicio;
		}
		public Integer getPuestoLlamada() {
			return puestoLlamada;
		}
		public void setPuestoLlamada(Integer puestoLlamada) {
			this.puestoLlamada = puestoLlamada;
		}
		public String getOrigen() {
			return origen;
		}
		public void setOrigen(String origen) {
			this.origen = origen;
		}
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
		public void setAsistio(Boolean asistio) {
			if (asistio){
			   this.asistio = "Si";
			}
			else {
				this.asistio= "No";
			}
		}
		public String getAsistio() {
			return asistio;
		}
		
}
