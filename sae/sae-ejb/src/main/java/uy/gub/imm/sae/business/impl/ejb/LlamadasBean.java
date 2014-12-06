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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.ConsultasLocal;
import uy.gub.imm.sae.business.api.LlamadasLocal;
import uy.gub.imm.sae.business.api.LlamadasRemote;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.entity.Atencion;
import uy.gub.imm.sae.entity.Llamada;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_PLANIFICADOR","RA_AE_FATENCION", "RA_AE_LLAMADOR"})
public class LlamadasBean implements LlamadasLocal, LlamadasRemote {

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;

	@EJB
	private LlamadasHelperLocal helper;	

	@EJB
	private ConsultasLocal consultas;
	
	static Logger logger = Logger.getLogger(LlamadasBean.class);
	
	
	@Resource
	private SessionContext ctx;
	
	
	
	
	public List<ReservaDTO> obtenerReservasEnEspera(Recurso recurso, List<Estado> estados) throws BusinessException {

		VentanaDeTiempo hoy = new VentanaDeTiempo();
		Date ahora = new Date();
		hoy.setFechaInicial(Utiles.time2InicioDelDia(ahora));
		hoy.setFechaFinal(Utiles.time2FinDelDia(ahora));
		
		if (estados.size() == 1 && estados.contains(Estado.R) ) {
			return consultas.consultarReservasEnEspera(recurso);
		}
		else if (estados.size() == 2 && estados.contains(Estado.R) && estados.contains(Estado.U)) {
			return consultas.consultarReservasEnEsperaUtilizadas(recurso);
		}
		else {
			return consultas.consultarReservasPorPeriodoEstado(recurso, hoy, estados);
		}
	
	}	
	
	
	/**
	 * Obtiene la siguiente reserva del dia en la lista de espera y 
	 * automáticamente le cambia el estado a U (Utilizada) y genera una llamada con la reserva y el puesto.
	 * Utiliza el campo VERSION en la entidad Rerserva para realizar un bloque optimista
	 * Si dos transacciones toman la misma reserva para cambiarle el estado, solo uno tendra exito, por 
	 * lo que este metodo, frente a una excepcion de bloqueo optimista vuelve a intenar con la
	 * siguiente reserva hasta obtener una o que se acaben las reservas en la lista de espera, 
	 * en cuyo caso retorna null.  
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	public Reserva siguienteEnEspera(Recurso recurso, Integer puesto) throws BusinessException {

		
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}
		 
		Reserva reserva = null;
		Integer recursoId = recurso.getId();
		
		//Lista de espera 
		Query query = em.createQuery(
				"select r.id " +
				"from   Reserva r " +
				"       join r.disponibilidades d " +
				"where   " +
				"       d.recurso.id = :recurso and " +
				"       r.estado = :estado and " +
				"       d.fecha = :hoy " +
				"order by d.fecha, d.horaInicio, r.id " 
				)
				.setParameter("recurso", recursoId)
				.setParameter("estado", Estado.R)
				.setParameter("hoy", new Date(), TemporalType.DATE);
		
		//List<Reserva> resultados = (List<Reserva>)query.getResultList();		
		List<Integer> resultados = (List<Integer>)query.getResultList();
		
		//Busco la siguiente reserva a ser llamada con mutua exlucion y lo logro al insertar la llamada, 
		//con el uso de clave de unicidad en la tabla de llamadas por id de la reserva.
		Boolean buscarSiguiente = true;
		//Iterator<Reserva> iter = resultados.iterator();
		Iterator<Integer> iter = resultados.iterator();
		while (buscarSiguiente && iter.hasNext()) {
		
			Integer reservaId = iter.next();
			//reserva = iter.next();
			//Integer reservaId = reserva.getId();
		
			try {
				reserva = helper.hacerLlamadaMutex(recursoId, reservaId, puesto);
				if (reserva != null) {
					buscarSiguiente = false;
				}
			} catch (EJBException e) {
				if (e.getCausedByException() instanceof OptimisticLockException) {
					logger.info("ACCESO MULTIPLE A RESERVA EN SIGUIETNE RESERVA (id = "+reservaId+") "+e.getMessage()+" "+e.getCause().getClass().toString());
				}
				else {
					throw e;
				}
			}
		}
		
		if ( ! buscarSiguiente ) {
			//Encontró
			return reserva;	
		}
		else {
			return null;
		}
		
	}
	
	/**
	 * Vuelve a colocar la reserva indicada para ser llamada, es decir, 
	 * simula el comportamiento del metodo siguienteEnEspera para una reserva que ya fue utilizada,
	 * generando una llamada con la reserva y el puesto.
	 * @throws BusinessException 
	 */
	public Reserva volverALlamar(Recurso recurso, Integer puesto, Reserva reserva) throws BusinessException {

		
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}


		Integer reservaId = reserva.getId();
		try {
			reserva = helper.reintentarLlamadaMutex(recurso.getId(), reservaId, puesto);
		} catch (EJBException e) {
			if (e.getCausedByException() instanceof OptimisticLockException) {
				logger.info("ACCESO MULTIPLE A RESERVA EN VOLVER A LLAMAR (id = "+reservaId+") "+e.getMessage()+" "+e.getCause().getClass().toString());
				reserva = null;
			}
			else {
				throw e;
			}
		}

		return reserva;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Llamada> obtenerLlamadas(List<Recurso> recursos, Integer cantLlamadas) throws BusinessException {

		List<Object[]> llamadas    = new ArrayList<Object[]>();
		List<Llamada> llamadasDTO = new ArrayList<Llamada>();
		
		if (recursos == null || recursos.isEmpty()) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}

		Query query = em.createQuery(
				"select ll.id, ll.etiqueta, ll.fecha, ll.hora, ll.numero, ll.puesto " +
				"from   Llamada ll " +
				"where   " +
				"       ll.recurso.id IN (:recursos) and " +
				"       ll.fecha = :hoy " +				
				"order by ll.id desc " 
		);
		List<Integer> recursosIds = new ArrayList<Integer>();
		for (Recurso recurso : recursos) {
			recursosIds.add(recurso.getId());
		}
		query.setParameter("recursos", recursosIds);
		query.setParameter("hoy", new Date(), TemporalType.DATE);
			
		query.setMaxResults(cantLlamadas);
		
		llamadas = (List<Object[]>)query.getResultList();
		
		Map<Integer, Integer> puestos = new HashMap<Integer, Integer>();
		for (Object[] llamada : llamadas) {
			Llamada dto = new Llamada();
			dto.setId((Integer)llamada[0]);
			dto.setEtiqueta((String)llamada[1]);
			dto.setFecha((Date)llamada[2]);
			dto.setHora((Date)llamada[3]);
			dto.setNumero((Integer)llamada[4]);
			
			if (puestos.containsKey(llamada[5])) {
				dto.setPuesto(null);
			}
			else {
				dto.setPuesto((Integer)llamada[5]);
				puestos.put((Integer)llamada[5], (Integer)llamada[5]);
			}
			llamadasDTO.add(dto);
		}
		
		return llamadasDTO;
	}

	/**
	 * Deja constancia de que el ciudadano asistio a la cita reservada.
	 * @throws BusinessException 
	 */
	public void marcarAsistencia(Recurso recurso, Reserva reserva) throws BusinessException {
		
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}
		if (reserva == null){
			throw new BusinessException("AE20085", "La reserva no puede ser nula");
		}

		Atencion atencion = new Atencion();
		atencion.setReserva(reserva);
		atencion.setAsistio(true);
		atencion.setDuracion(1);
		atencion.setFuncionario(ctx.getCallerPrincipal().getName());
		
		em.persist(atencion);
		
	}


	/**
	 * Deja constancia de que el ciudadano asistio a la cita reservada
	 * @throws BusinessException 
	 */
	public void marcarInasistencia(Recurso recurso, Reserva reserva) throws BusinessException {
		
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}

		if (reserva == null){
			throw new BusinessException("AE20085", "La reserva no puede ser nula");
		}

		Atencion atencion = new Atencion();
		atencion.setReserva(reserva);
		atencion.setAsistio(false);
		atencion.setDuracion(1);
		atencion.setFuncionario(ctx.getCallerPrincipal().getName());
		
		em.persist(atencion);
		
	}

}
