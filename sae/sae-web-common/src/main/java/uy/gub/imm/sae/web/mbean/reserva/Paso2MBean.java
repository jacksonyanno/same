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

package uy.gub.imm.sae.web.mbean.reserva;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.apache.log4j.Logger;
import org.richfaces.component.UIDataTable;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.RolException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.factories.BusinessLocatorFactory;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.Row;
import uy.gub.imm.sae.web.common.RowList;

public class Paso2MBean	extends PasoMBean{
	
	static Logger logger = Logger.getLogger(Paso2MBean.class);
		
	private AgendarReservas agendarReservasEJB;

	private UIDataTable tablaDispMatutina;
	private UIDataTable tablaDispVespertina;
	
	
	@PostConstruct
	public void init() {

		try {

			if(getEsIntranet()){
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getAgendarReservas();
			} else {	
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
			}
			
			if (sesionMBean.getAgenda() == null || sesionMBean.getRecurso() == null) {
				
				redirect(ESTADO_INVALIDO_PAGE_OUTCOME);
				return;
			}
			
			//Previene una recarga de la pagina o si el usuario oprime la tecla Back del navegador.
			//para minimizar la cantidad de reservas pendientes.

			if (sesionMBean.getReserva() != null) {
				getLogger().info("Desmarco Reserva:"+sesionMBean.getReserva().getId()+" "+sesionMBean.getReserva().getEstado()+ " "+sesionMBean.getReserva().getFechaCreacion());
				agendarReservasEJB.desmarcarReserva(sesionMBean.getReserva());
				sesionMBean.setReserva(null);
			}
			
			configurarDisponibilidadesDelDia();
		} catch (ApplicationException e) {
			logger.error("NO SE PUDO OBTENER AgendarReservas");
			logger.error(e);
			redirect(ERROR_PAGE_OUTCOME);
		} catch (Exception e) {
			addErrorMessage(e);
		}

	}

	public String getAgendaNombre() {
		
		String result = null;
		

		if (sesionMBean.getAgenda() != null) {
			result = sesionMBean.getAgenda().getDescripcion();
		}
		
		return (result == null ? "":result);
	}
	
	public String getRecursoDescripcion() {
		
		String result = null;
		

		if (sesionMBean.getRecurso() != null) {
			result = sesionMBean.getRecurso().getDescripcion();
		}
		
		return (result == null ? "":result);
	}



	public String getDescripcion() {
		
		String result = null;
		
		if (sesionMBean.getAgenda().getTextoAgenda() != null) {
			result = sesionMBean.getAgenda().getTextoAgenda().getTextoPaso2();
		}
		
		return (result == null ? "":result);
	}

	public String getDescripcionRecurso() {

		String result = null;

		if (sesionMBean.getRecurso().getTextoRecurso() != null) {
			result = sesionMBean.getRecurso().getTextoRecurso().getTextoPaso2();
		}
		
		return (result == null ? "":result);
	}

	public String getEtiquetaDelRecurso() {
		
		String result = null;

		if (sesionMBean.getAgenda().getTextoAgenda() != null) {
			result = sesionMBean.getAgenda().getTextoAgenda().getTextoSelecRecurso();
		}
		
		return (result == null ? "":result);
	}

	public Date getDiaSeleccionado() {
		return sesionMBean.getDiaSeleccionado();
	}
	
	public Boolean getHayDisponibilidadesMatutina() {
		return sesionMBean.getDisponibilidadesDelDiaMatutina().size() != 0;
	}

	public RowList<Disponibilidad> getDisponibilidadesMatutina() {
		return sesionMBean.getDisponibilidadesDelDiaMatutina();
	}

	public Boolean getHayDisponibilidadesVespertina() {
		return sesionMBean.getDisponibilidadesDelDiaVespertina().size() != 0;
	}

	public RowList<Disponibilidad> getDisponibilidadesVespertina() {
		return sesionMBean.getDisponibilidadesDelDiaVespertina();
	}

	public UIDataTable getTablaDispMatutina() {
		return tablaDispMatutina;
	}

	public void setTablaDispMatutina(UIDataTable tablaDispMatutina) {
		this.tablaDispMatutina = tablaDispMatutina;
	}

	public UIDataTable getTablaDispVespertina() {
		return tablaDispVespertina;
	}

	public void setTablaDispVespertina(UIDataTable tablaDispVespertina) {
		this.tablaDispVespertina = tablaDispVespertina;
	}


	public String reservarMatutina() {
		
		@SuppressWarnings("unchecked")
		Row<Disponibilidad> rDisp = (Row<Disponibilidad>)tablaDispMatutina.getRowData();
		sesionMBean.getDisponibilidadesDelDiaMatutina().setSelectedRow(rDisp);
		sesionMBean.getDisponibilidadesDelDiaVespertina().setSelectedRow(null);
		
		try {
			marcarReserva(rDisp.getData());
		} 
		catch (Exception e) {
			addErrorMessage(e);
			return null;
		}
		
		return "siguientePaso";
	}

	public String reservarVespertina() {
		
		@SuppressWarnings("unchecked")
		Row<Disponibilidad> rDisp = (Row<Disponibilidad>)tablaDispVespertina.getRowData();
		sesionMBean.getDisponibilidadesDelDiaVespertina().setSelectedRow(rDisp);
		sesionMBean.getDisponibilidadesDelDiaMatutina().setSelectedRow(null);
		
		try {
			marcarReserva(rDisp.getData());
		} 
		catch (Exception e) {
			addErrorMessage(e);
			return null;
		}
		
		return "siguientePaso";
	}

	private void marcarReserva(Disponibilidad d) throws RolException, BusinessException, UserException {
		
		Reserva r = agendarReservasEJB.marcarReserva(d);
		sesionMBean.setReserva(r);
		sesionMBean.setDisponibilidad(d);
	}

	private void configurarDisponibilidadesDelDia() {

		List<Disponibilidad> dispMatutinas   = new ArrayList<Disponibilidad>();
		List<Disponibilidad> dispVespertinas = new ArrayList<Disponibilidad>();

		if (sesionMBean.getDiaSeleccionado() != null) {
			
			VentanaDeTiempo ventana = new VentanaDeTiempo();
			ventana.setFechaInicial(Utiles.time2InicioDelDia(sesionMBean.getDiaSeleccionado()));
			ventana.setFechaFinal(Utiles.time2FinDelDia(sesionMBean.getDiaSeleccionado()));
			
			try {
				List<Disponibilidad> lista = agendarReservasEJB
							.obtenerDisponibilidades(sesionMBean.getRecurso(), ventana);

				for (Disponibilidad d : lista) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(d.getHoraInicio());
					
					if (cal.get(Calendar.AM_PM) == Calendar.AM) {
						//Matutino
						dispMatutinas.add(d);
					}
					else {
						//Vespertino
						dispVespertinas.add(d);
					}
				}
				
				
			} catch (Exception e) { 
				addErrorMessage(e);
			}
		}	

		sesionMBean.setDisponibilidadesDelDiaMatutina(new RowList<Disponibilidad>(dispMatutinas));
		sesionMBean.setDisponibilidadesDelDiaVespertina(new RowList<Disponibilidad>(dispVespertinas));
	}	
	
	
	public void beforePhase (PhaseEvent event) {
		disableBrowserCache(event);
		
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sesionMBean.limpiarPaso3();
		}
	}
	
	
}


