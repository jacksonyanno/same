/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.
 *
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

package com.sagant.same.business.impl.ejb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import uy.gub.imm.sae.business.api.AgendasLocal;
import uy.gub.imm.sae.business.api.DisponibilidadesLocal;
import uy.gub.imm.sae.business.api.RecursosLocal;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;

import com.sagant.same.business.api.WizzardLocal;
import com.sagant.same.business.api.WizzardRemote;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR"})
public class WizzardBean implements WizzardLocal, WizzardRemote {
	
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;
	
	@EJB
	private AgendasLocal agendasEJB;
	
	@EJB
	private DisponibilidadesLocal disponibilidadesEJB;

	@EJB
	private RecursosLocal recursosEJB;
	

	
	//Crea la nueva estructura de agenda en el grupo indicado.
	//Si el grupo no existe, el mismo sera creado siguiendo la estructura y datos pasados por parámetro
	//Retorna la nueva estructura completando nombres e ids.
	public Recurso crearAgenda (Recurso recurso) throws UserException, ApplicationException, BusinessException{


		Agenda wizzAgenda = agendasEJB.buscarAgendaPorNombre(recurso.getAgenda().getNombre());
		
		//Si no existe la agenda, la creo
		if (wizzAgenda == null) {

			wizzAgenda = agendasEJB.crearAgenda(new Agenda(recurso.getAgenda()));
			
			wizzAgenda.getTextoAgenda().setTextoPaso1(recurso.getAgenda().getTextoAgenda().getTextoPaso1());
			wizzAgenda.getTextoAgenda().setTextoPaso2(recurso.getAgenda().getTextoAgenda().getTextoPaso2());
			wizzAgenda.getTextoAgenda().setTextoPaso3(recurso.getAgenda().getTextoAgenda().getTextoPaso3());
			wizzAgenda.getTextoAgenda().setTextoSelecRecurso(recurso.getAgenda().getTextoAgenda().getTextoSelecRecurso());
			wizzAgenda.getTextoAgenda().setTextoTicketConf(recurso.getAgenda().getTextoAgenda().getTextoTicketConf());
			//wizzAgenda.getTextoAgenda().setAgenda(wizzAgenda);
		
		}

		
	
		Recurso wizzRecurso = recursosEJB.crearRecurso(wizzAgenda, new Recurso(recurso));
		
		for (AgrupacionDato agrupacionDato : recurso.getAgrupacionDatos()) {
			
			AgrupacionDato wizzAgrupacion = new AgrupacionDato(agrupacionDato);
			wizzAgrupacion.setId(null);
			
			wizzAgrupacion = recursosEJB.agregarAgrupacionDato(wizzRecurso,wizzAgrupacion);
			
			for (DatoASolicitar datoASolicitar : agrupacionDato.getDatosASolicitar()) {
				
				DatoASolicitar wizzDatoASolicitar = new DatoASolicitar(datoASolicitar);
				wizzDatoASolicitar.setId(null);
				
				wizzDatoASolicitar = recursosEJB.agregarDatoASolicitar(wizzRecurso, wizzAgrupacion, wizzDatoASolicitar);
			}
			
		}

		for (Disponibilidad disponibilidad : recurso.getDisponibilidades()) {
			disponibilidadesEJB.generarDisponibilidadesNuevas(wizzRecurso, disponibilidad.getFecha(), disponibilidad.getHoraInicio(), disponibilidad.getHoraFin(), disponibilidad.getDuracionEnMinutos(), disponibilidad.getCupo());
		}

		return wizzRecurso;
		
	}

	


}
