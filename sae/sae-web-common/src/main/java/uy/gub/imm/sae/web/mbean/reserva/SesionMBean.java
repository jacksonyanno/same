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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.RolException;
import uy.gub.imm.sae.common.factories.BusinessLocatorFactory;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.RowList;
import uy.gub.imm.sae.web.common.SAECalendarDataModel;

public class SesionMBean	extends BaseMBean {
	
	static Logger logger = Logger.getLogger(SesionMBean.class);
	
	private AgendarReservas agendarReservasEJB;

	private String paginaDeRetorno;
	private Boolean soloCuerpo = false;
	
	private Agenda agenda;
	private Recurso recurso;
	private Map<String, DatoASolicitar> datosASolicitar;
	
	//Calendario de la agenda para el recurso elegido
    private SAECalendarDataModel calendario;
	private VentanaDeTiempo ventanaCalendario;
	private VentanaDeTiempo ventanaMesSeleccionado;
	private List<Integer> cuposXdiaMesSeleccionado;
    private Date currentDate;
	
	private Date diaSeleccionado;
	//TODO pasar las listas de disponibilidades matutina y vespertina de RowList a List
	private RowList<Disponibilidad> disponibilidadesDelDiaMatutina;
	private RowList<Disponibilidad> disponibilidadesDelDiaVespertina;

	private Disponibilidad disponibilidad;
	private Reserva reserva;
	private Reserva reservaConfirmada;
	
	//Maneja el estado en el paso 3 sobre confirmar una reserva y volver a confirmar (con cancelacion de resevas previas) si la misma presenta clave única repetida.
	private Boolean cancelarReservasPrevias = false;
	//Maneja el estado en el paso 3 sobre confirmar una reserva y volver a confirmar aunque las validaciones den warnings.	
	private Boolean confirmarConWarning = false;
	
	@PostConstruct
	public void init() {

		try {
			if(getEsIntranet()){
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getAgendarReservas();
			} else {	
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
			}
			
		} catch (ApplicationException e) {
			logger.error("NO SE PUDO OBTENER EJB AgendarReservas");
			logger.error(e);
			redirect(ERROR_PAGE_OUTCOME);			
		}
	}	
	
	public void seleccionarAgenda(String agendaNombre) throws BusinessException, RolException, ApplicationException {

		limpiarSesion();
		agenda = agendarReservasEJB.consultarAgendaPorNombre(agendaNombre);
	}
	

	/*
	public String getUrlPruebaAgendaConducir() {
		String s = "http://localhost:8080/SAE-Internet-WEB/";
		String sEncoded = "";
		try {
			sEncoded = URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "/SAE-Internet-WEB/agendarReserva/Paso1.xhtml?agenda=CONDUCIR&pagina_retorno="+sEncoded;
	}

	public String getUrlPruebaAgendaConducirSoloCuerpo() {
		String s = "http://localhost:8080/SAE-Internet-WEB/";
		String sEncoded = "";
		try {
			sEncoded = URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "/SAE-Internet-WEB/agendarReserva/Paso1.xhtml?agenda=CONDUCIR&pagina_retorno="+sEncoded+"&solo_cuerpo=true";
	}*/
	
	public String getPaginaDeRetorno() {
		return paginaDeRetorno;
	}
	
	public void setPaginaDeRetorno(String paginaDeRetorno) {
		this.paginaDeRetorno = paginaDeRetorno;
	}
	
	
	public Boolean getSoloCuerpo() {
		return soloCuerpo;
	}

	public void setSoloCuerpo(Boolean soloCuerpo) {
		this.soloCuerpo = soloCuerpo;
	}


	public Agenda getAgenda() {
		return agenda;
	}

	public Recurso getRecurso() {
		return recurso;
	}

	public void setRecurso(Recurso recurso) {
		this.recurso = recurso;
	}

	public Date getDiaSeleccionado() {
		return diaSeleccionado;
	}

	public void setDiaSeleccionado(Date diaSeleccionado) {

		if (diaSeleccionado == null) {
			this.diaSeleccionado = null;
			this.disponibilidadesDelDiaMatutina = null;
			this.disponibilidadesDelDiaVespertina = null;
			this.disponibilidad = null;
		}
		else {
		
			if (this.diaSeleccionado == null || !diaSeleccionado.equals(this.diaSeleccionado)) {

				this.diaSeleccionado = diaSeleccionado;
				this.disponibilidadesDelDiaMatutina = null;
				this.disponibilidadesDelDiaVespertina = null;
				this.disponibilidad = null;
			}
		}
	}

	public RowList<Disponibilidad> getDisponibilidadesDelDiaMatutina() {
		return disponibilidadesDelDiaMatutina;
	}

	public void setDisponibilidadesDelDiaMatutina(RowList<Disponibilidad> disponibilidadesDelDiaRows) {
		this.disponibilidadesDelDiaMatutina = disponibilidadesDelDiaRows;
	}

	public RowList<Disponibilidad> getDisponibilidadesDelDiaVespertina() {
		return disponibilidadesDelDiaVespertina;
	}

	public void setDisponibilidadesDelDiaVespertina(RowList<Disponibilidad> disponibilidadesDelDiaRows) {
		this.disponibilidadesDelDiaVespertina = disponibilidadesDelDiaRows;
	}
	
	public Disponibilidad getDisponibilidad() {
		return disponibilidad;
	}

	public void setDisponibilidad(Disponibilidad disponibilidad) {
		this.disponibilidad = disponibilidad;
	}
	
	public Reserva getReserva() {
		return reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}
	
	public Reserva getReservaConfirmada() {
		return reservaConfirmada;
	}

	public void setReservaConfirmada(Reserva reservaConfirmada) {
		this.reservaConfirmada = reservaConfirmada;
	}


	public SAECalendarDataModel getCalendario() {
		return calendario;
	}
	public void setCalendario(SAECalendarDataModel calendario) {
		this.calendario = calendario;
	}
	
	/**
	 * El calendario usa este atributo para saber que en mes posicionarse, 
	 * inicialmente se posiciona en el mes correspondiente a la fecha inicial de la
	 * la ventana del calendario de la agenda, pero luego mediante el setCurrentDate
	 * el componente grafico actualiza esta fecha según nos movamos en los meses/años
	 * del calendario. No confundir con el dia seleccionado en el calendario. 
	 */
	public Date getCurrentDate() {
		return currentDate;
	}	
	public void setCurrentDate(Date date) {
		currentDate = date;
	}

	public VentanaDeTiempo getVentanaCalendario() {
		return ventanaCalendario;
	}

	public void setVentanaCalendario(VentanaDeTiempo ventanaCalendario) {
		this.ventanaCalendario = ventanaCalendario;
	}

	public VentanaDeTiempo getVentanaMesSeleccionado() {
		return ventanaMesSeleccionado;
	}

	public void setVentanaMesSeleccionado(VentanaDeTiempo ventanaMesSeleccionado) {
		this.ventanaMesSeleccionado = ventanaMesSeleccionado;
	}

	public List<Integer> getCuposXdiaMesSeleccionado() {
		return cuposXdiaMesSeleccionado;
	}

	public void setCuposXdiaMesSeleccionado(List<Integer> cuposXdiaMesSeleccionado) {
		this.cuposXdiaMesSeleccionado = cuposXdiaMesSeleccionado;
	}

	public void setDatosASolicitar(Map<String, DatoASolicitar> datos) {
		this.datosASolicitar = datos;
	}

	public Map<String, DatoASolicitar> getDatosASolicitar() {
		return this.datosASolicitar;
	}
	
	public Boolean getCancelarReservasPrevias() {
		return cancelarReservasPrevias;
	}


	public void setCancelarReservasPrevias(Boolean cancelarReservasPrevias) {
		this.cancelarReservasPrevias = cancelarReservasPrevias;
	}

	public Boolean getConfirmarConWarning() {
		return confirmarConWarning;
	}

	public void setConfirmarConWarning(Boolean confirmarConWarning) {
		this.confirmarConWarning = confirmarConWarning;
	}


	private void limpiarSesion() {

		agenda = null;
		recurso = null;
		datosASolicitar = null;
		
	    calendario= null;
		ventanaCalendario = null;
		ventanaMesSeleccionado = null;
		cuposXdiaMesSeleccionado = null;
	    currentDate = null;
		
		diaSeleccionado = null;

		limpiarPaso2();
	}

	public void limpiarPaso2() {

		disponibilidadesDelDiaMatutina = null;
		disponibilidadesDelDiaVespertina = null;

		disponibilidad = null;
		reserva = null;
		
		limpiarPaso3();
	}

	public void limpiarPaso3() {
		cancelarReservasPrevias = false;
		confirmarConWarning = false;
//		reservaConfirmada = null;
	}


}


