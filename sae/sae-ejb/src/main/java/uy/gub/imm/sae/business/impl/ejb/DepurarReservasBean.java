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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.DepurarReservasLocal;
import uy.gub.imm.sae.business.api.DepurarReservasRemote;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

@Stateless
@RolesAllowed("RA_AE_ANONIMO")
public class DepurarReservasBean implements DepurarReservasLocal,
		DepurarReservasRemote {
	private static Logger logger = Logger.getLogger(DepurarReservasBean.class);
	private static final int MINUTOS_BORRAR_RESERVAS_PENDIENTES =30; 
	private static final int MINUTOS_INTERVALO_EJECUCION = 10;
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;

	@Resource
	private SessionContext ctx;

	/* Elimina todas las Reservas que estan en estado
	 * Pendiente desde un periodo de tiempo ya transcurrido
	 */ 
	
	@SuppressWarnings("unchecked")
	@Timeout
	public void eliminarReservasPendientes(Timer timer){
		
		/* Busco reservas en estado pendientes creadas hace mes tiempo 
		 * que el valor dado por la constante MINUTOS_BORRAR_RESERVAS_PENDIENTES  
		 */ 
		Query query =  em.createQuery(
		" select r from Reserva r" +
		" where " +
		"    r.estado='P' and " +
		"	(EXTRACT(DAY FROM (CURRENT_TIMESTAMP - fcrea))*24*60 + " +
		"    EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - fcrea))*60 + " + 
		"    EXTRACT(MINUTE FROM (CURRENT_TIMESTAMP - fcrea)))> :periodoBorrado ");
		
		query.setParameter("periodoBorrado", MINUTOS_BORRAR_RESERVAS_PENDIENTES);
		
		List<Reserva> reservaLista = (List<Reserva>) query.getResultList();
	
		for (Reserva reserva : reservaLista){
			em.remove(reserva);
		}
		logger.info("Se eliminaron " + reservaLista.size() + " reservas Pendientes .... ");
		
	
	}
	
	@RolesAllowed("RA_AE_ANONIMO")
	public void initTimers() {
		logger.info("Arranco inicializacion del timer ...");
		TimerService timerService = ctx.getTimerService();

		logger.info("obtenemos los timers actuales del bean ...");
		@SuppressWarnings("unchecked")
		Collection<Timer> timersDelBean = timerService.getTimers();
		for (Iterator<Timer> iterator = timersDelBean.iterator(); iterator.hasNext();) {
			Timer unTimerDelBean = iterator.next();
			logger.info("matamos un timer del bean ...");
			unTimerDelBean.cancel();
		}

		logger.info("creamos el nuevo timer ...");
		//long initialInterval = 30000;
		long initialInterval = new Long(MINUTOS_INTERVALO_EJECUCION)*60*1000;
		long intervalToRepeat = (MINUTOS_INTERVALO_EJECUCION*60*1000);
		timerService.createTimer(initialInterval, intervalToRepeat,
				"Timer de Generacion para elminar reservas pendientes");
		logger.info("Inicialice el timer ...");
	}


}
