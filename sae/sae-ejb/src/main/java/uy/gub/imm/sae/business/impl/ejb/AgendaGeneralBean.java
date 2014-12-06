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

package uy.gub.imm.sae.business.impl.ejb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import uy.gub.imm.sae.business.api.AgendaGeneralLocal;
import uy.gub.imm.sae.business.api.AgendaGeneralRemote;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.Enumerados.SAERol;
import uy.gub.imm.sae.entity.Enumerados.SAERolPrefijo;
import uy.gub.imm.sae.entity.Plantilla;
import uy.gub.imm.sae.entity.Recurso;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_PLANIFICADOR","RA_AE_FCALL_CENTER","RA_AE_ANONIMO", "RA_AE_FATENCION", "RA_AE_LLAMADOR"})
//@EJB(name = "java:global/MyBean", beanInterface = AgendaGeneralRemote.class)
public class AgendaGeneralBean implements AgendaGeneralLocal, AgendaGeneralRemote {
	
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;
	
	@Resource
	private SessionContext ctx;

	/**
	 * Retorna una lista de agendas vivas (fechaBaja == null) ordenada por nombre
	 * Solo retorna las agendas para las que el usuario tenga rol  Administrador/Planificador .
	 * Roles permitidos: Administrador, Planificador
	 */
	@SuppressWarnings("unchecked")
	public List<Agenda> consultarAgendas() throws ApplicationException{
		try{
			//Se obtienen todas las agendas para las cuales el usuario tiene
			//algún rol.
			List<Agenda> agendas = (List<Agenda>) entityManager
									.createQuery("SELECT distinct a " +
												 "FROM Agenda a " +
												 "WHERE a.fechaBaja IS NULL " +
												 "ORDER BY a.descripcion")
									.getResultList();
			
			List<Agenda> agendasConPermiso = new ArrayList<Agenda>(); 
			
			for (Iterator<Agenda> iterator = agendas.iterator(); iterator.hasNext();) {
				Agenda agenda = (Agenda) iterator.next();
				
				if(ctx.isCallerInRole(SAERol.RA_AE_ADMINISTRADOR.toString())){
					// si tiene permiso de administrador, puede ver todo
					agendasConPermiso.add(agenda);
				} else if(ctx.isCallerInRole(SAERolPrefijo.RA_AE_PLANI.toString()+"_"+agenda.getNombre())) {
					// si tiene rol planificador para la agenda
					agendasConPermiso.add(agenda);
				} else if(ctx.isCallerInRole(SAERolPrefijo.RA_AE_FATEN.toString()+"_"+agenda.getNombre())) {
					// si tiene rol funcionario de atencion para la agenda
					agendasConPermiso.add(agenda);
				}else if(ctx.isCallerInRole(SAERolPrefijo.RA_AE_FCALL.toString()+"_"+agenda.getNombre())) {
					// si tiene rol funcionario de atencion para la agenda
					agendasConPermiso.add(agenda);
				}
			}
			
			return agendasConPermiso;
			
		} catch (Exception e){
			throw new ApplicationException(e);
		}
	}

	/**
	 * Retorna una lista de recursos vivos (fechaBaja == null)
	 * Controla que el usuario tenga rol Administrador/Planificador sobre la agenda <b>a</b>
	 * Roles permitidos: Administrador, Planificador
	 */
	@SuppressWarnings("unchecked")
	public List<Recurso> consultarRecursos(Agenda a) throws ApplicationException{
		try{
			List<Recurso> recurso = (List<Recurso>) entityManager
									.createQuery("SELECT r from Recurso r " +
											"WHERE r.agenda = :a " +
											"AND r.fechaBaja IS NULL "+
											"ORDER BY r.nombre")
									.setParameter("a", a)
									// TODO CONTROLAR ROLES
									.getResultList();
			return recurso;
			} catch (Exception e){
				throw new ApplicationException(e);
			}
	}

	/**
	 * Retorna una lista de plantillas vivas (fechaBaja == null)
	 * ordenadas por orden de creacion
	 * Controla que el usuario tenga rol Administrador/Planificador sobre la agenda del recurso <b>r</b>
	 * Roles permitidos: Administrador, Planificador
	 */
	@SuppressWarnings("unchecked")
	public List<Plantilla> consultarPlantillas(Recurso r) throws ApplicationException{
		try{
			List<Plantilla> plantilla = (List<Plantilla>) entityManager
									.createQuery("SELECT p from Plantilla p " +
											"WHERE p.recurso = :r " +
											"AND p.fechaBaja IS NULL ")
									.setParameter("r",r)
									// TODO CONTROLAR ROLES
									.getResultList();
			return plantilla;
			} catch (Exception e){
				throw new ApplicationException(e);
			}
	}

	
}
