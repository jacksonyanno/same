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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.entity.Llamada;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_PLANIFICADOR","RA_AE_FATENCION"})
public class LlamadasHelperBean implements LlamadasHelperLocal {

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;

	
	static Logger logger = Logger.getLogger(LlamadasHelperBean.class);
	
	
	/**
	 * Persiste una nueva llamdada y marca la reserva como utilizada,
	 * realiza mutua exclusion sobre la reserva utilizando bloqueo optimista con @ Version, 
	 * Se asume que no existe otra llamada para la misma reserva
	 * (unicidad sobre la clave foranea de Reserva).
	 * Realiza todo en una transacción independiente. 
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Reserva hacerLlamadaMutex(Integer recursoId, Integer reservaId, Integer puesto) {

		Recurso recurso = em.find(Recurso.class, recursoId);
		Reserva reserva = em.find(Reserva.class, reservaId);

		if (reserva.getEstado().equals(Estado.R)) {
			
			Llamada ll = buildLlamada(recurso, reserva, puesto);
	
			reserva.setEstado(Estado.U);
			em.flush();
			em.persist(ll); //Despues del flush pues si no salta unicidad en lugar de lockeo optimista

			reserva.getDatosReserva().size(); //Lazy initialization
			reserva.getDisponibilidades().size(); //Lazy initialization
			return reserva;
		} 
		else {
			return null;
		}
	}

	
	/**
	 * Persiste una llamada para una reserva que ya fue llamada, es decir, es un reintento.
	 * Realiza mutua exclusion sobre la llamada en el sentido que si se realiza la misma llamada dara violacion de integridad referencial
	 * pues no puden haber 2 llamadas para una misma reserva. 
	 * Por lo que asume que puede existir una llamada para esta reserva, la que debe ser borrada antes de persistir la nueva llamada.
	 * Realiza todo en una transacción independiente. 
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Reserva reintentarLlamadaMutex(Integer recursoId, Integer reservaId, Integer puesto) {

		Recurso recurso = em.find(Recurso.class, recursoId);
		Reserva reserva = em.find(Reserva.class, reservaId);

		//Como la reserva tiene @Version, utilizara bloqueo optimista.
		//Debido a que en realidad no modifico la reserva, necesito hacer un lock explicito para asegurar mutua exlusion en la llamada.
		//es decir, para forzar al control de optimistic lockin sobre la reserva.
		em.lock(reserva, LockModeType.WRITE);
		
		if (reserva.getEstado().equals(Estado.U)) {
			
			//Busco y borro la llamada previa, si existe aún.
			Llamada anteriorLlamada = (Llamada)em.createQuery(
					"select ll " +
					"from   Llamada ll " +
					"where   " +
					"       ll.reserva.id = :reserva "
					)
					.setParameter("reserva", reservaId)
					.getSingleResult();		
			
			if (anteriorLlamada != null) {
				em.remove(anteriorLlamada);		
				em.flush();
			}
			
			Llamada ll = buildLlamada(recurso, reserva, puesto);
			em.persist(ll);
			em.flush();

			reserva.getDatosReserva().size(); //Lazy initialization
			reserva.getDisponibilidades().size(); //Lazy initialization
			return reserva;
		} 
		else {
			return null;
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	private Llamada buildLlamada(Recurso recurso, Reserva reserva, Integer puesto) {
	
		Llamada ll = new Llamada();
		ll.setReserva(reserva);
		ll.setRecurso(recurso);
		ll.setFecha(reserva.getDisponibilidades().get(0).getFecha());
		ll.setHora(reserva.getDisponibilidades().get(0).getHoraInicio());
		ll.setNumero(reserva.getNumero());
		ll.setPuesto(puesto);

		
		
		//Obtengo los datos solicitados para la reserva que deben desplegarse en la pantalla llamadora
		List<Object[]> datos = (List<Object[]>) em.createQuery(
									"select dr.datoASolicitar.incluirEnLlamador, dr.datoASolicitar.largoEnLlamador, dr.valor " +
									"from DatoReserva dr " +
									"where dr.reserva = :reserva " +
									"order by dr.datoASolicitar.ordenEnLlamador ")
									.setParameter("reserva", reserva)
									.getResultList();
		
		String etiqueta = "";
		for (Object[] row : datos) {
			Boolean incluir = (Boolean)row[0];
			Integer largo = (Integer)row[1];
			String valor = (String)row[2];
			
			if (incluir) {
				if (valor.length() <= largo) {
					etiqueta += valor;
					etiqueta += " ";
				}
				else {
					etiqueta += valor.substring(0,largo);
					etiqueta += ". ";
				}
			}
		}
		
/*		
		//Obtengo los datos solicitados para la reserva que deben desplegarse en la pantalla llamadora
		List<DatoReserva> datos = (List<DatoReserva>) em.createQuery(
									"select dr " +
									"from DatoReserva dr " +
									"where dr.reserva = :reserva " +
									"order by dr.datoASolicitar.ordenEnLlamador ")
									.setParameter("reserva", reserva)
									.getResultList();
		
		String etiqueta = "";
		for (DatoReserva datoReserva : datos) {
			if (datoReserva.getDatoASolicitar().getIncluirEnLlamador()) {
				int largo = datoReserva.getDatoASolicitar().getLargoEnLlamador();
				if (datoReserva.getValor().length() <= largo) {
					etiqueta += datoReserva.getValor();
					etiqueta += " ";
				}
				else {
					etiqueta += datoReserva.getValor().substring(0,largo);
					etiqueta += ". ";
				}
			}
		}
*/		
		if (etiqueta.equals("")) {
			ll.setEtiqueta("---");
		}
		else {
			ll.setEtiqueta(etiqueta);
		}
		
		logger.info("ETIQUETA:"+ll.getEtiqueta());
		return ll;
	}
	
}
