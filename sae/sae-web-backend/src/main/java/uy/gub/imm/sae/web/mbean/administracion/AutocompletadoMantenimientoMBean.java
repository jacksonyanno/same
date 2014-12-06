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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;

import org.richfaces.component.html.HtmlDataTable;
import org.richfaces.component.html.HtmlDatascroller;

import uy.gub.imm.sae.business.api.Autocompletados;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.ModoAutocompletado;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.entity.ParametrosAutocompletar;
import uy.gub.imm.sae.entity.ServicioAutocompletar;
import uy.gub.imm.sae.web.common.BaseMBean;

public class AutocompletadoMantenimientoMBean extends BaseMBean {

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/AutocompletadosBean")
	private Autocompletados autocompletadosEJB;

	private SessionMBean sessionMBean;
	private AutocompletadoMantenimientoSessionMBean autocompletadoMantenimientoSessionMBean;

	
	private List<ServicioAutocompletar> autocompletados;
	private HtmlDataTable autocompletadosTable;
	
	private HtmlDataTable parametrosTable;
	private HtmlDatascroller parametrosDataScroller;
	
	
	@PostConstruct
	public void init() {
		if (autocompletadosEJB  == null) autocompletadosEJB = (Autocompletados)lookupEJB(SAEProfile.getInstance().EJB_AUTOCOMPLETADOS_JNDI);
	}
	
	
	
	public void beforePhase(PhaseEvent event){
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("acciones.autocompletar.update.title"));
		}
		
	}
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}


	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}



	public AutocompletadoMantenimientoSessionMBean getAutocompletadoMantenimientoSessionMBean() {
		return autocompletadoMantenimientoSessionMBean;
	}


	public void setAutocompletadoMantenimientoSessionMBean(
			AutocompletadoMantenimientoSessionMBean autocompletadoMantenimientoSessionMBean) {
		this.autocompletadoMantenimientoSessionMBean = autocompletadoMantenimientoSessionMBean;
	}


	public List<ServicioAutocompletar> getAutocompletados() {
		
		autocompletados = new ArrayList<ServicioAutocompletar>();
		
		try {
			autocompletados = autocompletadosEJB.consultarAutoCompletados();
		} catch(Exception e) {
			addErrorMessage(e,MSG_ID);
		}
		
		return autocompletados;
	}


	public void setAutocompletados(List<ServicioAutocompletar> autocompletados) {
		this.autocompletados = autocompletados;
	}

	public HtmlDataTable getAutocompletadosTable() {
		return autocompletadosTable;
	}

	public void setAutocompletadosTable(HtmlDataTable autocompletadosTable) {
		this.autocompletadosTable = autocompletadosTable;
	}

	public void eliminarAutocompletado(ActionEvent event) {
		ServicioAutocompletar auto = (ServicioAutocompletar)getAutocompletadosTable().getRowData();
		
		try {
			autocompletadosEJB.eliminarAutoCompletado(auto);
			autocompletadoMantenimientoSessionMBean.setModoCreacion(false);
			autocompletadoMantenimientoSessionMBean.setModoEdicion(false);
			autocompletadoMantenimientoSessionMBean.setAutocompletado(null);
		}
		catch (Exception e) {
			addErrorMessage(e,MSG_ID);
		}
	}
	
	
	public HtmlDataTable getParametrosTable() {
		return parametrosTable;
	}

	public void setParametrosTable(HtmlDataTable parametrosTable) {
		this.parametrosTable = parametrosTable;
	}
	
	public HtmlDatascroller getParametrosDataScroller() {
		return parametrosDataScroller;
	}

	public void setParametrosDataScroller(HtmlDatascroller parametrosDataScroller) {
		this.parametrosDataScroller = parametrosDataScroller;
	}

	public List<SelectItem> getTiposDeDato() {
		
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (Tipo t : Tipo.values()) {
			items.add(new SelectItem(t, t.getDescripcion(getI18N().getLocale())));
		}
		
		return items;
	}
	
	
	public List<SelectItem> getModo() {
		
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (ModoAutocompletado m : ModoAutocompletado.values()) {
			items.add(new SelectItem(m, m.getDescripcion()));		
		}		
		return items;
	}
	

	public void editar(ActionEvent event) {

		ServicioAutocompletar s = (ServicioAutocompletar)getAutocompletadosTable().getRowData();
		
		autocompletadoMantenimientoSessionMBean.setModoEdicion(true);
		autocompletadoMantenimientoSessionMBean.setModoCreacion(false);
		
		List<ParametrosAutocompletar> parametros = new ArrayList<ParametrosAutocompletar>();
		try {
			parametros = autocompletadosEJB.consultarParametrosDelAutoCompletado(s);
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}

		autocompletadoMantenimientoSessionMBean.setAutocompletado(s);
		autocompletadoMantenimientoSessionMBean.getAutocompletado().setParametrosAutocompletados(parametros);
		
	}
	
	public void guardarEdicion(ActionEvent event) {

		try {
			
			//Modifica la validacion y sus parametros
			autocompletadosEJB.modificarAutoCompletado(autocompletadoMantenimientoSessionMBean.getAutocompletado());
			
			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			autocompletadoMantenimientoSessionMBean.setModoEdicion(false);
			autocompletadoMantenimientoSessionMBean.setAutocompletado(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarEdicion(ActionEvent e) {
		autocompletadoMantenimientoSessionMBean.setModoEdicion(false);
		autocompletadoMantenimientoSessionMBean.setAutocompletado(null);
	}

	public void crear(ActionEvent event) {
		
		ServicioAutocompletar s = new ServicioAutocompletar();
		
		autocompletadoMantenimientoSessionMBean.setModoEdicion(false);
		autocompletadoMantenimientoSessionMBean.setModoCreacion(true);
		
		autocompletadoMantenimientoSessionMBean.setAutocompletado(s);
	}

	public void guardarCreacion(ActionEvent event) {

		try {
			
			autocompletadosEJB.crearAutoCompletado(autocompletadoMantenimientoSessionMBean.getAutocompletado());

			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			autocompletadoMantenimientoSessionMBean.setModoCreacion(false);
			autocompletadoMantenimientoSessionMBean.setAutocompletado(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarCreacion(ActionEvent e) {
		autocompletadoMantenimientoSessionMBean.setModoCreacion(false);
		autocompletadoMantenimientoSessionMBean.setAutocompletado(null);
	}
	
	
	public void crearParametro(ActionEvent event) {
		autocompletadoMantenimientoSessionMBean.getAutocompletado().getParametrosAutocompletados().add(new ParametrosAutocompletar());
		int size = autocompletadoMantenimientoSessionMBean.getAutocompletado().getParametrosAutocompletados().size();
		autocompletadoMantenimientoSessionMBean.setParametrosTablePageIndex(Double.valueOf(size / parametrosTable.getRows()).intValue()+1);
		parametrosDataScroller.setPage(Double.valueOf(size / parametrosTable.getRows()).intValue()+1);
	}
	
	public void eliminarParametro(ActionEvent event) {
		
		int i = getParametrosTable().getRowIndex();
		autocompletadoMantenimientoSessionMBean.getAutocompletado().getParametrosAutocompletados().remove(i);
	}
}
