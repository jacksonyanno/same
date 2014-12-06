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

import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import uy.gub.imm.sae.business.api.AgendaGeneral;
import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.Llamadas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.Llamada;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.RowMultipleSelect;
import uy.gub.imm.sae.web.common.TipoMonitor;

public class LlamadorMBean extends BaseMBean {

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	@EJB(name="ejb/LlamadasBean")
	private Llamadas llamadasEJB;
	
	@EJB(name="ejb/AgendarReservasBean")
	private AgendarReservas agendarReservasEJB;
	
	@EJB(name="ejb/AgendaGeneralBean")
	private AgendaGeneral generalEJB;

	private LlamadorSessionMBean llamadorSessionMBean;
	private SessionMBean sessionMBean;

	private List<Llamada> llamadas;
	private Map<Integer,Boolean> mapNuevasLlamadas;
	private Boolean hayNuevaLlamada;
	
	// Variables para redirigir al llamador
	private static final String URL_BASE_TO_FORWARD_LLAMADOR = "/administracion/llamador/listaDeLlamadas.xhtml?agenda=";
	
	@PostConstruct
	public void init() {
		
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (llamadasEJB == null) llamadasEJB = (Llamadas)lookupEJB(SAEProfile.getInstance().EJB_LLAMADAS_JNDI);
		if (generalEJB  == null) generalEJB  = (AgendaGeneral)lookupEJB(SAEProfile.getInstance().EJB_AGENDA_GENERAL_JNDI);
		if (agendarReservasEJB == null) agendarReservasEJB = (AgendarReservas)lookupEJB(SAEProfile.getInstance().EJB_AGENDAR_RESERVAS_JNDI);

		
		//Se controla que se haya Marcado una agenda para trabajar con los recursos
		if (sessionMBean.getAgendaMarcada() == null){
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		if (sessionMBean.getRecursoMarcado() != null) {
			try {
				
				if (llamadorSessionMBean.getMostrarDatos() == null) {
					llamadorSessionMBean.setMostrarDatos(
							recursosEJB.mostrarDatosASolicitarEnLlamador(sessionMBean.getRecursoMarcado())
					);
				}

			} catch (Exception e) {
				addErrorMessage(new ApplicationException(e), MSG_ID);
			}
		}
	}
	
	
	public LlamadorSessionMBean getLlamadorSessionMBean() {
		return llamadorSessionMBean;
	}

	public void setLlamadorSessionMBean(LlamadorSessionMBean llamadorSessionMBean) {
		this.llamadorSessionMBean = llamadorSessionMBean;
	}

	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}

	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}
	
	
	/** Configura titulo pantalla de configuracion del llamador cuando se accede desde la administracion */
	public void beforePhaseConfiguracionLlamador(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo("Configuración del Llamador");
			sessionMBean.getRecursosMultipleSelect().getSelectedRows().clear();
		}
		
	}
	
	/** Valida parametros para armar el llamador generico */
	public void beforePhaseListaDeLlamadas(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			try {
				Map<String, String> parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
				if (parametros.get("agenda") != null && parametros.get("recursos") != null && parametros.get("tipoMonitor") != null) {					
					
					Agenda agenda = agendarReservasEJB.consultarAgendaPorNombre(parametros.get("agenda"));
					if (agenda == null) {
						throw new InvalidParameterException(getI18N().getText("message.agenda_not_exist"));
					}
					
					List<String> recursosParam = Arrays.asList(parametros.get("recursos").split(","));
					if (recursosParam.isEmpty()) {
						throw new InvalidParameterException(getI18N().getText("message.invalid_resource"));
					}
					List<Recurso> recursosAgenda = generalEJB.consultarRecursos(agenda);					
					List<Recurso> recursos = new ArrayList<Recurso>();
					for(Recurso r : recursosAgenda) {
						if(recursosParam.contains(r.getNombre())) {
							recursos.add(r);
						}
					}
					
					llamadorSessionMBean.setRecursos(recursos);
					TipoMonitor tipoMonitor = TipoMonitor.fromPulgadas(Integer.valueOf(parametros.get("tipoMonitor")));
					llamadorSessionMBean.setTipoMonitor( 
							(tipoMonitor != null ? tipoMonitor :  TipoMonitor.fromPulgadas(Integer.valueOf("22"))) 
							);
				}
			}
			catch (Exception e) {
				addErrorMessage(new ApplicationException(e), MSG_ID);
			}
		}
	}
	
	public String[] getPulgadasMonitor() {
		return new String[] {llamadorSessionMBean.getTipoMonitor().getPulgadas().toString()};
	}

	public Integer getCantLlamadas() {
		return llamadorSessionMBean.getTipoMonitor().getLineas();
	}


	public String getNombreAgenda() {
		if (sessionMBean.getAgendaMarcada() != null) {
			return sessionMBean.getAgendaMarcada().getDescripcion().toUpperCase();
		}
		else {
			return null;
		}
	}
	
	/** Si se configura el llamador para mostrar 1 solo recurso se muestra el nombre. */
	public boolean getMostrarDescripcionRecurso() {
		if (sessionMBean.getRecursosMultipleSelect() != null) {
			return sessionMBean.getRecursosMultipleSelect().getSelectedRows().size() == 1;
		}
		return true;
	}

	public String getDescripcionRecurso() {
		if (sessionMBean.getRecursoMarcado() != null) {
			return sessionMBean.getRecursoMarcado().getDescripcion().toUpperCase();
		}
		else {
			return null;
		}
	}
	
	public String getNombreColumnaPuesto() {
		if (sessionMBean.getRecursoMarcado() != null) {
			return sessionMBean.getRecursoMarcado().getTextoRecurso().getTituloPuestoEnLlamador();
		}
		else {
			return null;
		}
	}
	
	public String getNombreColumnaDatos() {
		if (sessionMBean.getRecursoMarcado() != null) {
			return sessionMBean.getRecursoMarcado().getTextoRecurso().getTituloCiudadanoEnLlamador();
		}
		else {
			return null;
		}
	}
	
	
	public Boolean getMostrarNumero() {
		if (sessionMBean.getRecursoMarcado() != null) {
			return sessionMBean.getRecursoMarcado().getMostrarNumeroEnLlamador();
		}
		else {
			return false;
		}
	}

	public Boolean getMostrarDatos() {
		return llamadorSessionMBean.getMostrarDatos();
	}

	public List<Llamada> getLlamadas() {
		if (llamadas == null && sessionMBean.getRecursosMultipleSelect().getSelectedRows() != null) {
			try {
				llamadas = llamadasEJB.obtenerLlamadas(llamadorSessionMBean.getRecursos(), getCantLlamadas());
			} catch (Exception e) {
				addErrorMessage(new ApplicationException(e), MSG_ID);
			}
		}
		
		return llamadas;
	}
	

	
	/*
	 * Determina si hay nueva llamada desde el ultimo refresco
	 */
	public void refrescarLlamadas(ActionEvent e) {
		
	
		mapNuevasLlamadas = new HashMap<Integer, Boolean>();
	
		List<Llamada> llamadasAnteriores = llamadorSessionMBean.getLlamadas();

		//Recorro las llamadas recien obtenidas hasta el maximo de despligue
		// si no las encuentro en la lista de llamadas anteriores:
		//   la tomo como nueva, agregando el indice a la lista de indices.
		for (int i = 0; i< getCantLlamadas(); i++) {
			
			if (getLlamadas() != null && getLlamadas().size() > i) {
				
				boolean hayNuevaLlamada = true;
				
				if (llamadasAnteriores != null) {
					
					Llamada llamadaNew = getLlamadas().get(i);

					for (Llamada llamadaOld : llamadasAnteriores) {
						if (llamadaNew.getId().equals(llamadaOld.getId())) {
							hayNuevaLlamada = false;
						}
					}
				}
				
				mapNuevasLlamadas.put(i, hayNuevaLlamada);
			}
			else {
				mapNuevasLlamadas.put(i, false);
			}
		}

		hayNuevaLlamada = mapNuevasLlamadas.containsValue(true);
		llamadorSessionMBean.setLlamadas(getLlamadas());
	}

	public Boolean getHayNuevaLlamada() {
		return hayNuevaLlamada;
	}

	public Map<Integer, Boolean> getMapNuevasLlamadas() {
		return mapNuevasLlamadas;
	}
	
	/**
	 * Abre el llamador pasando como parámetro vía url los datos necesarios.
	 * Se pensó así para automatizar la carga del llamador en los locales coemrciales mediante login script.
	 * @return
	 */
	public void abrirLlamador() {
		
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();
					
		StringBuffer urlLlamador = new StringBuffer(request.getContextPath() + URL_BASE_TO_FORWARD_LLAMADOR);
		
		if (sessionMBean.getAgendaMarcada() != null) {
			try {
					
				StringBuilder recursos = new StringBuilder();
				for (Iterator<RowMultipleSelect<Recurso>> iterator = sessionMBean.getRecursosMultipleSelect().getSelectedRows().iterator(); iterator.hasNext();) {
			        recursos.append(((RowMultipleSelect<Recurso>) iterator.next()).getData().getNombre());
			        if (iterator.hasNext()) recursos.append(",");
			    }
				urlLlamador.append(sessionMBean.getAgendaMarcada().getNombre())
					.append("&recursos=")
					.append(URLEncoder.encode(recursos.toString(), "utf-8"))
					.append("&tipoMonitor=")
					.append(URLEncoder.encode(llamadorSessionMBean.getTipoMonitor().getPulgadas().toString(), "utf-8"));
				
				FacesContext.getCurrentInstance().getExternalContext().redirect(urlLlamador.toString());
				
			} catch (Exception e) {
				addErrorMessage(new ApplicationException(e), MSG_ID);
			}
		}
		else {
			addErrorMessage("Debe tener una agenda seleccionada", MSG_ID);
		}
	}
	
	/** 
	 * Retorna la lista de monitores para configurar el llamador 
	*/
	public List<SelectItem> getTiposDeMonitores() {
		List<SelectItem> listaMonitores = new ArrayList<SelectItem>();
		for (TipoMonitor tipoMonitor : TipoMonitor.values()) {
			listaMonitores.add(new SelectItem(tipoMonitor.getPulgadas(), tipoMonitor.getEtiqueta()));
		}
		return listaMonitores;
	}
	
	public void cambioTipoMonitor(ValueChangeEvent e) {
		llamadorSessionMBean.setTipoMonitor(TipoMonitor.fromPulgadas(Integer.valueOf(e.getNewValue().toString())));
	}
}