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

package uy.gub.imm.sae.web.mbean.administracion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ErrorValidacionCommitException;
import uy.gub.imm.sae.common.exception.ErrorValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionClaveUnicaException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionPorCampoException;
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.FormularioDinamicoReserva;

/**
 * Maneja la lógica de generación dinámica de los componentes gráficos que
 * constituyen los datos a solicitarse para realizar la reserva.
 * @author im2716295
 *
 */
public class DisenioFormularioMBean extends BaseMBean {

	public static final String FORMULARIO_ID = "datosReserva";
	public static final String DATOS_RESERVA_MBEAN = "datosReservaMBean";
	
	@EJB(name="ejb/AgendarReservasBean")
	private AgendarReservas agendarReservasEJB;

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private UIComponent campos;
	private UIComponent camposError;
	private Map<String, Object> datosReservaMBean;
	
	private FormularioDinamicoReserva formularioDin;
	
	private SessionMBean sessionMBean;

	@PostConstruct
	public void init() {
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (agendarReservasEJB == null) agendarReservasEJB = (AgendarReservas)lookupEJB(SAEProfile.getInstance().EJB_AGENDAR_RESERVAS_JNDI);
	}	
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}

	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	public UIComponent getCampos() {
		return campos;
	}
	
	public void setCampos(UIComponent campos) {
		
		this.campos = campos;

		try {
			Recurso recurso = sessionMBean.getRecursoMarcado();

			//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
			//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
			if (campos.getChildCount() == 0 && recurso != null) {
				
				if (formularioDin == null) {
					List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
					sessionMBean.setDatosASolicitar(obtenerCampos(agrupaciones));
					formularioDin = new FormularioDinamicoReserva(DATOS_RESERVA_MBEAN, FORMULARIO_ID, FormularioDinamicoReserva.TipoFormulario.EDICION);
					
					//TODO: VER ESTOOO!!!!!!
					formularioDin.armarFormulario(agrupaciones,null);
				}
				UIComponent formulario = formularioDin.getComponenteFormulario();
				campos.getChildren().add(formulario);
			}
		} catch (Exception e) {
			addErrorMessage(e);
		}
	}

	public UIComponent getCamposError() {
		return camposError;
	}

	public void setCamposError(UIComponent camposError) {
		this.camposError = camposError;

		try {
			Recurso recurso = sessionMBean.getRecursoMarcado();

			//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
			//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
			if (camposError.getChildCount() == 0 && recurso != null) {
				
				if (formularioDin == null) {
					List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
					sessionMBean.setDatosASolicitar(obtenerCampos(agrupaciones));
					formularioDin = new FormularioDinamicoReserva(DATOS_RESERVA_MBEAN, FORMULARIO_ID, FormularioDinamicoReserva.TipoFormulario.EDICION);
					
					//TODO: VER ESTOOO!!!!!!
					formularioDin.armarFormulario(agrupaciones,null);
				}
				UIComponent errores = formularioDin.getComponenteMensajes();
				camposError.getChildren().add(errores);
			}
		} catch (Exception e) {
			addErrorMessage(e);
		}
	}

	public Map<String,Object> getDatosReservaMBean() {
		return datosReservaMBean;
	}

	public void setDatosReservaMBean(Map<String,Object> datosReservaMBean) {
		this.datosReservaMBean = datosReservaMBean;
	}

	public void refrescar(ActionEvent event) {
		
		getCampos().getChildren().clear();
		try {
			Recurso recurso = sessionMBean.getRecursoMarcado();

			//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
			//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
			if (recurso != null) {
				List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
				sessionMBean.setDatosASolicitar(obtenerCampos(agrupaciones));
				FormularioDinamicoReserva formularioDin = new FormularioDinamicoReserva(DATOS_RESERVA_MBEAN, FORMULARIO_ID, FormularioDinamicoReserva.TipoFormulario.EDICION);
				
				//TODO: VER ESTOOO!!!!!!
				formularioDin.armarFormulario(agrupaciones, null);
				UIComponent formulario = formularioDin.getComponenteFormulario();
				campos.getChildren().add(formulario);
			}
		} catch (Exception e) {
			addErrorMessage(e);
		}		
	}
	
	public void testearValidaciones(ActionEvent event) {	

		try {
			
			List<String> idComponentes = new ArrayList<String>();
			
			List<DatoReserva> datos = new ArrayList<DatoReserva>();
			
			for (String nombre : datosReservaMBean.keySet()) {
				Object valor = datosReservaMBean.get(nombre);

				idComponentes.add(nombre);
				
				if (valor != null && ! valor.toString().equals("")) {
					DatoReserva dato = new DatoReserva();
					dato.setDatoASolicitar(sessionMBean.getDatosASolicitar().get(nombre));
					//TODO DatoReserva implemetar correctamente el parser de object a string para cada tipo.
					dato.setValor(valor.toString());
					datos.add(dato);
				}
			}
			
			FormularioDinamicoReserva.desmarcarCampos(idComponentes, campos);
			
			try {
				agendarReservasEJB.validarDatosReserva(sessionMBean.getRecursoMarcado(), datos);
			}
			catch (ValidacionPorCampoException e) {
				//Alguno de los campos no tiene el formato esperado

				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					if (e.getMensaje(i) != null) {
						addErrorMessage(e.getMensaje(i), FORM_ID + ":" + e.getNombreCampo(i));
					}
					idComponentesError.add(e.getNombreCampo(i));
				}
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				return;
			}
			catch (ErrorValidacionException e) {
				//Algun grupo de campos no es valido según alguna validacion

				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addErrorMessage(mensaje, FORM_ID + ":" + FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return;
			}
			catch (WarningValidacionException e) {
				//Algun grupo de campos tiene algun valor que amerita una atencion.
				
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addInfoMessage(mensaje, FORM_ID + ":" + FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return;				
			}
			catch (ErrorValidacionCommitException e) {
				//Algun grupo de campos no es valido seg�n alguna validacion

				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addErrorMessage(mensaje, FORM_ID + ":" + FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return;
			}
			catch (WarningValidacionCommitException e) {
				//Algun grupo de campos tiene algun valor que amerita una atencion.
				
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addInfoMessage(mensaje, FORM_ID + ":" + FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return;				
			}
			catch (ValidacionClaveUnicaException e) {
				//Campos clave repetidos para otra reserva
				
				addErrorMessage(e.getMessage(), FORM_ID + ":" + FORMULARIO_ID);
				
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);

				return;
				
			}
			catch (ValidacionException e) {
				//Faltan campos requeridos
				
				addErrorMessage(e.getMessage(), FORM_ID + ":" + FORMULARIO_ID);
			
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);

				return;
			}

			addInfoMessage(getI18N().getText("formulario.field.validations"));
			
		} catch (Exception e) {
			addErrorMessage(e);
			return;
		}
	}

	
	/**
	 * @param agrupaciones de algun recurso
	 * @return retorna todos los datos a solicitar definidos en la lista de agrupaciones 
	 *         en un mapa cuya clave es el nombre del campo 
	 */
	private Map<String, DatoASolicitar> obtenerCampos(List<AgrupacionDato> agrupaciones) {
		
		Map<String, DatoASolicitar> camposXnombre = new HashMap<String, DatoASolicitar>();
		
		for (AgrupacionDato agrupacion : agrupaciones) {
			for (DatoASolicitar dato : agrupacion.getDatosASolicitar()) {
				camposXnombre.put(dato.getNombre(), dato);
			}
		}
		
		return camposXnombre;
	}
	
	
}

	
	
