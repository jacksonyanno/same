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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.enumerados.ModoAutocompletado;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.AutocompletarException;
import uy.gub.imm.sae.common.exception.ErrorAutocompletarException;
import uy.gub.imm.sae.common.exception.ErrorValidacionCommitException;
import uy.gub.imm.sae.common.exception.ErrorValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionClaveUnicaException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionPorCampoException;
import uy.gub.imm.sae.common.exception.WarningAutocompletarException;
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.common.factories.BusinessLocatorFactory;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.ParametrosAutocompletar;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.ServicioAutocompletarPorDato;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.web.common.FormularioDinamicoReserva;

/**
 * Maneja la lógica de generación dinámica de los componentes gráficos que
 * constituyen los datos a solicitarse para realizar la reserva.
 * @author im2716295
 *
 */
public class Paso3MBean extends PasoMBean {

	static Logger logger = Logger.getLogger(Paso3MBean.class);
	public static final String FORMULARIO_ID = "datosReserva";
	public static final String DATOS_RESERVA_MBEAN = "datosReservaMBean";
	
	private AgendarReservas agendarReservasEJB;

	private Recursos recursosEJB;
	

	private UIComponent campos;
	private UIComponent camposError;
	private Map<String, Object> datosReservaMBean;
	private FormularioDinamicoReserva formularioDin;
	
	private String textoCaptcha;
	

	public void beforePhase (PhaseEvent event) {
		disableBrowserCache(event);
		
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			if (sesionMBean.getReserva() == null) {
				//Se ha apretado el boton de back o algun acceso directo
				FacesContext ctx = FacesContext.getCurrentInstance();
				ctx.getApplication().getNavigationHandler().handleNavigation(ctx, "", "pasoAnterior");
			}
		}
	}
	

	@PostConstruct
	public void init() {

		try {

			if(getEsIntranet()){
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getAgendarReservas();
				recursosEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getRecursos();
			} else {	
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
				recursosEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getRecursos();
			}
			
			if (sesionMBean.getAgenda() == null || sesionMBean.getRecurso() == null) {
				redirect(ESTADO_INVALIDO_PAGE_OUTCOME);
				return;
			}
		} catch (ApplicationException e) {
			logger.error("NO SE PUDO OBTENER EJBs");
			logger.error(e);
			redirect(ERROR_PAGE_OUTCOME);
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
			result = sesionMBean.getAgenda().getTextoAgenda().getTextoPaso3();
		}

		return (result == null ? "":result);
	}

	public String getDescripcionRecurso() {
		
		String result = null;
		
		if (sesionMBean.getRecurso().getTextoRecurso() != null) {
			result = sesionMBean.getRecurso().getTextoRecurso().getTextoPaso3();
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
	
	public Date getHoraSeleccionada() {
		if (sesionMBean.getDisponibilidad() != null) {
			return sesionMBean.getDisponibilidad().getHoraInicio();
		}
		else {
			return null;
		}
	}
	
	
	public UIComponent getCampos() {
		return campos;
	}
	
	public void setCampos(UIComponent campos) {
		
		this.campos = campos;

		try {
			Recurso recurso = sesionMBean.getRecurso();

			//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
			//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
			if (campos.getChildCount() == 0 && recurso != null) {
				
				if (formularioDin == null) {
					List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
					sesionMBean.setDatosASolicitar(obtenerCampos(agrupaciones));
					formularioDin = new FormularioDinamicoReserva(DATOS_RESERVA_MBEAN, FORMULARIO_ID, FormularioDinamicoReserva.TipoFormulario.EDICION);
					
					
					HashMap<Integer,HashMap<Integer,ServicioPorRecurso>> serviciosAutocompletar = new HashMap<Integer,HashMap<Integer,ServicioPorRecurso>>();
						
					if (getEsIntranet()){	
						List<ServicioPorRecurso> lstServiciosPorRecurso = recursosEJB.consultarServicioAutocompletar(recurso);
						
						for (ServicioPorRecurso sRec : lstServiciosPorRecurso){
							List<ServicioAutocompletarPorDato> lstDatos = sRec.getAutocompletadosPorDato();
							List<ParametrosAutocompletar> parametros = sRec.getAutocompletado().getParametrosAutocompletados();
							
							DatoASolicitar ultimo = null;
							for (ParametrosAutocompletar param : parametros){
								if (ModoAutocompletado.SALIDA.equals(param.getModo())){
									for (ServicioAutocompletarPorDato sDato : lstDatos){
										if (sDato.getNombreParametro().equals(param.getNombre())){
											
											if (ultimo == null){
												
												ultimo = sDato.getDatoASolicitar();
												
											}else {
												
												if (sDato.getDatoASolicitar().getAgrupacionDato().getOrden().intValue() > ultimo.getAgrupacionDato().getOrden().intValue()){
													
													HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = serviciosAutocompletar.get(ultimo.getId());
													if (auxServiciosRecurso.size() > 1){
														auxServiciosRecurso.remove(sRec.getId());
													}else{
														serviciosAutocompletar.remove(ultimo.getId());
													}
													ultimo = sDato.getDatoASolicitar();
													
												}else if (sDato.getDatoASolicitar().getAgrupacionDato().getOrden().intValue() == ultimo.getAgrupacionDato().getOrden().intValue()){
													if (sDato.getDatoASolicitar().getFila().intValue() > ultimo.getFila().intValue()){
														
														HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = serviciosAutocompletar.get(ultimo.getId());
														if (auxServiciosRecurso.size() > 1){
															auxServiciosRecurso.remove(sRec.getId());
														}else{
															serviciosAutocompletar.remove(ultimo.getId());
														}
														ultimo = sDato.getDatoASolicitar();
														
													}else if (sDato.getDatoASolicitar().getFila().intValue() == ultimo.getFila().intValue()){
														if (sDato.getDatoASolicitar().getColumna().intValue() > ultimo.getColumna().intValue()){
															
															HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = serviciosAutocompletar.get(ultimo.getId());
															if (auxServiciosRecurso.size() > 1){
																auxServiciosRecurso.remove(sRec.getId());
															}else{
																serviciosAutocompletar.remove(ultimo.getId());
															}
															ultimo = sDato.getDatoASolicitar();
															
														}
													}
												}
											}
											
											if (serviciosAutocompletar.containsKey(ultimo.getId())){
												serviciosAutocompletar.get(ultimo.getId()).put(sRec.getId(), sRec);
											}else{
												HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = new HashMap<Integer, ServicioPorRecurso>();
												auxServiciosRecurso.put(sRec.getId(), sRec);
												serviciosAutocompletar.put(ultimo.getId(),auxServiciosRecurso);
											}
											
											
										}
									}
								}
							}
						}
						formularioDin.armarFormulario(agrupaciones, serviciosAutocompletar);
					}else{
						formularioDin.armarFormulario(agrupaciones, null);
					}
				}
				UIComponent formulario = formularioDin.getComponenteFormulario();
				campos.getChildren().add(formulario);
			}
		} catch (Exception e) {
			addErrorMessage(e, FORMULARIO_ID);
			
		}
	}
	
	

	public UIComponent getCamposError() {
		return camposError;
	}


	public void setCamposError(UIComponent camposError) {
		this.camposError = camposError;

		try {
			Recurso recurso = sesionMBean.getRecurso();

			//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
			//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
			if (camposError.getChildCount() == 0 && recurso != null) {
				
				if (formularioDin == null) {
					List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
					sesionMBean.setDatosASolicitar(obtenerCampos(agrupaciones));
					formularioDin = new FormularioDinamicoReserva(DATOS_RESERVA_MBEAN, FORMULARIO_ID, FormularioDinamicoReserva.TipoFormulario.EDICION);
					
					HashMap<Integer,HashMap<Integer,ServicioPorRecurso>> serviciosAutocompletar = new HashMap<Integer,HashMap<Integer,ServicioPorRecurso>>();
					
					if (getEsIntranet()){	
						List<ServicioPorRecurso> lstServiciosPorRecurso = recursosEJB.consultarServicioAutocompletar(recurso);
						
						for (ServicioPorRecurso sRec : lstServiciosPorRecurso){
							List<ServicioAutocompletarPorDato> lstDatos = sRec.getAutocompletadosPorDato();
							List<ParametrosAutocompletar> parametros = sRec.getAutocompletado().getParametrosAutocompletados();
							
							DatoASolicitar ultimo = null;
							for (ParametrosAutocompletar param : parametros){
								if (ModoAutocompletado.SALIDA.equals(param.getModo())){
									for (ServicioAutocompletarPorDato sDato : lstDatos){
										if (sDato.getNombreParametro().equals(param.getNombre())){
											
											if (ultimo == null){
												
												ultimo = sDato.getDatoASolicitar();
												
											}else {
												
												if (sDato.getDatoASolicitar().getAgrupacionDato().getOrden().intValue() > ultimo.getAgrupacionDato().getOrden().intValue()){
													
													HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = serviciosAutocompletar.get(ultimo.getId());
													if (auxServiciosRecurso.size() > 1){
														auxServiciosRecurso.remove(sRec.getId());
													}else{
														serviciosAutocompletar.remove(ultimo.getId());
													}
													ultimo = sDato.getDatoASolicitar();
													
												}else if (sDato.getDatoASolicitar().getAgrupacionDato().getOrden().intValue() == ultimo.getAgrupacionDato().getOrden().intValue()){
													if (sDato.getDatoASolicitar().getFila().intValue() > ultimo.getFila().intValue()){
														
														HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = serviciosAutocompletar.get(ultimo.getId());
														if (auxServiciosRecurso.size() > 1){
															auxServiciosRecurso.remove(sRec.getId());
														}else{
															serviciosAutocompletar.remove(ultimo.getId());
														}
														ultimo = sDato.getDatoASolicitar();
														
													}else if (sDato.getDatoASolicitar().getFila().intValue() == ultimo.getFila().intValue()){
														if (sDato.getDatoASolicitar().getColumna().intValue() > ultimo.getColumna().intValue()){
															
															HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = serviciosAutocompletar.get(ultimo.getId());
															if (auxServiciosRecurso.size() > 1){
																auxServiciosRecurso.remove(sRec.getId());
															}else{
																serviciosAutocompletar.remove(ultimo.getId());
															}
															ultimo = sDato.getDatoASolicitar();
															
														}
													}
												}
											}
											
											if (serviciosAutocompletar.containsKey(ultimo.getId())){
												serviciosAutocompletar.get(ultimo.getId()).put(sRec.getId(), sRec);
											}else{
												HashMap<Integer,ServicioPorRecurso> auxServiciosRecurso = new HashMap<Integer, ServicioPorRecurso>();
												auxServiciosRecurso.put(sRec.getId(), sRec);
												serviciosAutocompletar.put(ultimo.getId(),auxServiciosRecurso);
											}
											
											
										}
									}
								}
							}
						}
						formularioDin.armarFormulario(agrupaciones, serviciosAutocompletar);
					}else{
						formularioDin.armarFormulario(agrupaciones, null);
					}
				}
				UIComponent errores = formularioDin.getComponenteMensajes();
				camposError.getChildren().add(errores);
			}
		} catch (Exception e) {
			addErrorMessage(e, FORMULARIO_ID);
		}
	}


	public Map<String,Object> getDatosReservaMBean() {
		return datosReservaMBean;
	}

	public void setDatosReservaMBean(Map<String,Object> datosReservaMBean) {
		this.datosReservaMBean = datosReservaMBean;
	}

	public String getTextoCaptcha() {
		return textoCaptcha;
	}

	public void setTextoCaptcha(String textoCaptcha) {
		this.textoCaptcha = textoCaptcha;
	}

	public String getTextoBotonConfirmar() {
		
		String result = null;
		
		if (sesionMBean.getCancelarReservasPrevias()) {
			result = getI18N().getText("reservaweb.step.3.button.update.label");
		}
		else {
			result = getI18N().getText("reservaweb.step.3.button.confirm.label");
		}
		
		return (result == null ? "":result);

	}
	
	public String confirmarReserva() {
		
		boolean cancelarReservasPrevias = sesionMBean.getCancelarReservasPrevias();
		sesionMBean.setCancelarReservasPrevias(false);

		boolean confirmarConWarning = sesionMBean.getConfirmarConWarning();
		sesionMBean.setConfirmarConWarning(false);
		
		try {
			
			List<String> idComponentes = new ArrayList<String>();
			
			Set<DatoReserva> datos = new HashSet<DatoReserva>();
			
			for (String nombre : datosReservaMBean.keySet()) {
				Object valor = datosReservaMBean.get(nombre);

				idComponentes.add(nombre);
				
				if (valor != null && ! valor.toString().trim().equals("")) {
					DatoReserva dato = new DatoReserva();
					dato.setDatoASolicitar(sesionMBean.getDatosASolicitar().get(nombre));
					//TODO DatoReserva implemetar correctamente el parser de object a string para cada tipo.
					dato.setValor(valor.toString());
					datos.add(dato);
				}
			}
			
			FormularioDinamicoReserva.desmarcarCampos(idComponentes, campos);
			
			Reserva r = sesionMBean.getReserva();
			r.setDatosReserva(datos);
		
			
			try {
				
				Boolean confirmada = false;
				while (!confirmada) {
					try {
						Reserva rConfiramda = agendarReservasEJB.confirmarReserva(r, cancelarReservasPrevias, confirmarConWarning);
						r.setNumero(rConfiramda.getNumero());
						confirmada = true;
					} catch (AccesoMultipleException e){
						//Reintento hasta tener existo, en algun momento no me va a dar acceso multiple.
					}
				}
/*				falta en el paso3.xhtml un boton cancelar que salta en caso de error de clave o waring y que resetee tanto los 2 atirbutos
				de sesion para poder darle la opcion al usuario de modificar y probar de nuevo.
*/
//				agendarReservasEJB.confirmarReservaCancelandoPrevias(sesionMBean.getReserva());
				
				//La reserva se confirm�, por lo tanto muevo la reseva a confirmada en la sesion para evitar problemas de reload de pagina.
				sesionMBean.setReservaConfirmada(r);
				sesionMBean.setReserva(null);
			}
			catch (ValidacionPorCampoException e) {
				//Alguno de los campos no tiene el formato esperado

				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					if (e.getMensaje(i) != null) {
						addErrorMessage(e.getMensaje(i), e.getNombreCampo(i));
					}
					idComponentesError.add(e.getNombreCampo(i));
				}
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				return null;
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
				addErrorMessage(mensaje, FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return null;
			}
			catch (WarningValidacionException e) {
				//Algun grupo de campos tiene algun valor que amerita una atencion.
				sesionMBean.setConfirmarConWarning(true);
				//Guardo estado de la cancelacion de reservas previas
				sesionMBean.setCancelarReservasPrevias(cancelarReservasPrevias);
				
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addInfoMessage(mensaje, FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return null;				
			}
			catch (ErrorValidacionCommitException e) {
				//Algun grupo de campos no es valido según alguna validacion

				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addErrorMessage(mensaje, FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return null;
			}
			catch (WarningValidacionCommitException e) {
				//Algun grupo de campos tiene algun valor que amerita una atencion.
				sesionMBean.setConfirmarConWarning(true);
				//Guardo estado de la cancelacion de reservas previas
				sesionMBean.setCancelarReservasPrevias(cancelarReservasPrevias);
				
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				String mensaje = e.getMensaje(0);
				for(int i = 1; i < e.getCantMensajes(); i++) {
					mensaje += "  |  "+e.getMensaje(i);
				}
				addInfoMessage(mensaje, FORMULARIO_ID);
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
				
				return null;				
			}
			catch (ValidacionClaveUnicaException e) {
				//Campos clave repetidos para otra reserva
				sesionMBean.setCancelarReservasPrevias(true);
				//Guardo el estado de la confirmacion con warnings
				sesionMBean.setConfirmarConWarning(confirmarConWarning);
				
				addErrorMessage(e.getMessage(), FORMULARIO_ID);
				
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);

				return null;
				
			}
			catch (ValidacionException e) {
				//Faltan campos requeridos
				
				addErrorMessage(e.getMessage(), FORMULARIO_ID);
			
				List<String> idComponentesError = new ArrayList<String>();
				for(int i = 0; i < e.getCantCampos(); i++) {
					idComponentesError.add(e.getNombreCampo(i));
				}
				FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);

				return null;
			}

			//Blanqueo el formulario de datos de la reserva
			datosReservaMBean.clear();

		} catch (Exception e) {
			addErrorMessage(e, FORMULARIO_ID);
			return null;
		}
		
		return "reservaConfirmada";
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
	
	public String autocompletarCampo() {
		
		Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		
		String claves = (String)requestParameterMap.get("paramIdsServicio");
		
		try {
			List<String> idComponentes = new ArrayList<String>();
			
			for (String nombre : datosReservaMBean.keySet()) {
				idComponentes.add(nombre);
			}
			
			FormularioDinamicoReserva.desmarcarCampos(idComponentes, campos);			
			
			String[] arrParamIdServicio = claves.split("\\|");
			
			for (String paramIdServicio : arrParamIdServicio){
				ServicioPorRecurso sRec = new ServicioPorRecurso();
				sRec.setId(new Integer(paramIdServicio));
				
				Map<String, Object> valoresAutocompletar = this.agendarReservasEJB.autocompletarCampo(sRec,datosReservaMBean);
				
				for (String nombre : valoresAutocompletar.keySet()) {
					
					datosReservaMBean.put(nombre, valoresAutocompletar.get(nombre).toString());
				}
			}
			
		} catch (ErrorAutocompletarException e) {
			List<String> idComponentesError = new ArrayList<String>();
			for(int i = 0; i < e.getCantCampos(); i++) {
				idComponentesError.add(e.getNombreCampo(i));
			}
			String mensaje = e.getMensaje(0);
			for(int i = 1; i < e.getCantMensajes(); i++) {
				mensaje += "  |  "+e.getMensaje(i);
			}
			addErrorMessage(mensaje, FORMULARIO_ID);
			FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
			
			return null;
		} catch (WarningAutocompletarException e) {
			
			List<String> idComponentesError = new ArrayList<String>();
			for(int i = 0; i < e.getCantCampos(); i++) {
				idComponentesError.add(e.getNombreCampo(i));
			}
			String mensaje = e.getMensaje(0);
			for(int i = 1; i < e.getCantMensajes(); i++) {
				mensaje += "  |  "+e.getMensaje(i);
			}
			addInfoMessage(mensaje, FORMULARIO_ID);
			FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);
			
			return null;
		} catch (AutocompletarException e) {
			//Faltan campos requeridos
			addErrorMessage(e.getMessage(), FORMULARIO_ID);
		
			List<String> idComponentesError = new ArrayList<String>();
			for(int i = 0; i < e.getCantCampos(); i++) {
				idComponentesError.add(e.getNombreCampo(i));
			}
			FormularioDinamicoReserva.marcarCamposError(idComponentesError, campos);

			return null;
		} catch (Exception e) {
			addErrorMessage(e, FORMULARIO_ID);
			return null;
		}
		
		return null;
	}
}

	
	
