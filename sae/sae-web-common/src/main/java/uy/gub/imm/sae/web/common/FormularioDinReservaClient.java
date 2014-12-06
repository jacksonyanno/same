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

package uy.gub.imm.sae.web.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.component.UIComponent;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

public class FormularioDinReservaClient {

	private static final String FORMULARIO_ID = "datosReserva";
	private static final String DATOS_FILTRO_RESERVA_MBEAN = "datosFiltroReservaMBean";

	
	public static void armarFormularioLecturaDinamico(Recurso recurso, Reserva reserva, UIComponent campos, List<AgrupacionDato> agrupaciones) throws Exception{
		
		/*		Recurso recurso = sessionMBean.getRecursoMarcado();
				Reserva reserva = consultaSessionMBean.getReserva();
		*/
				//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
				//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
				if (campos.getChildCount() == 0 && recurso != null && (!reserva.getDatosReserva().isEmpty())) {
				//	List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
					Map<String, Object> valores = obtenerValores(reserva.getDatosReserva());
					FormularioDinamicoReserva formularioDin = new FormularioDinamicoReserva(valores);
					formularioDin.armarFormulario(agrupaciones,null);
					UIComponent formulario = formularioDin.getComponenteFormulario();
					campos.getChildren().add(formulario);
				}
		}

	
	public static void armarFormularioEdicionDinamico(Recurso recurso,
			UIComponent filtroConsulta, List<AgrupacionDato> agrupaciones)
			throws Exception {
	
		// El chequeo de recurso != null es en caso de un acceso directo a la
		// pagina, es solo para que no salte la excepcion en el log, pues de todas formas sera
		// redirigido a una pagina de error.
		if (filtroConsulta.getChildCount() == 0 && recurso != null) {
			//setDatosASolicitar(FormularioDinamicoReserva.obtenerCampos(agrupaciones));
			FormularioDinamicoReserva formularioDin = new FormularioDinamicoReserva(
					DATOS_FILTRO_RESERVA_MBEAN, FORMULARIO_ID, FormularioDinamicoReserva.TipoFormulario.EDICION_CONSULTA);
			formularioDin.armarFormulario(agrupaciones,null);
			UIComponent formulario = formularioDin.getComponenteFormulario();
			filtroConsulta.getChildren().add(formulario);
		}
	}

			private static Map<String, Object> obtenerValores(Set<DatoReserva> datos) {
				
				Map<String, Object> valores = new HashMap<String, Object>();
				
				for (DatoReserva dato : datos) {
					//TODO parsear el valor de string a object segun el tipo del DatoASolicitar
					valores.put(dato.getDatoASolicitar().getNombre(), dato.getValor());
				}
				
				return valores;
			 }

			public static List<DatoReserva> obtenerDatosReserva(Map<String, Object> origen, Map<String, DatoASolicitar> datosASolicitar) {

				List<DatoReserva> datos = new ArrayList<DatoReserva>();

				for (String nombre : origen.keySet()) {
					Object valor = origen.get(nombre);

					if (valor != null && !valor.toString().equals("") && !valor.toString().equals("NoSeleccion")) {
						DatoReserva dato = new DatoReserva();
						dato.setDatoASolicitar(datosASolicitar.get(nombre));
						// TODO DatoReserva implemetar correctamente el parser de object
						// a string para cada tipo.
						dato.setValor(valor.toString());
						datos.add(dato);
					}
				}
				return datos;
			}



	
	
}
