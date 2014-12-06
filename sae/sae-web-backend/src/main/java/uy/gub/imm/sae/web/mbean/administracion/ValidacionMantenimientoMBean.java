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

import uy.gub.imm.sae.business.api.Validaciones;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.entity.ParametroValidacion;
import uy.gub.imm.sae.entity.Validacion;
import uy.gub.imm.sae.web.common.BaseMBean;

public class ValidacionMantenimientoMBean extends BaseMBean {

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/ValidacionesBean")
	private Validaciones validacionEJB;

	private SessionMBean sessionMBean;
	private ValidacionMantenimientoSessionMBean validacionMantenimientoSessionMBean;

	
	private List<Validacion> validaciones;
	private HtmlDataTable validacionesTable;
	
	private HtmlDataTable parametrosTable;
	private HtmlDatascroller parametrosDataScroller;
	
	
	
	@PostConstruct
	public void init() {
		if (validacionEJB == null) validacionEJB = (Validaciones)lookupEJB(SAEProfile.getInstance().EJB_VALIDACIONES_JNDI);

	}
	
	
	public void beforePhase(PhaseEvent event){
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("acciones.validar.update.title"));
		}
		
	}
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}


	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}



	public ValidacionMantenimientoSessionMBean getValidacionMantenimientoSessionMBean() {
		return validacionMantenimientoSessionMBean;
	}


	public void setValidacionMantenimientoSessionMBean(
			ValidacionMantenimientoSessionMBean validacionMantenimientoSessionMBean) {
		this.validacionMantenimientoSessionMBean = validacionMantenimientoSessionMBean;
	}


	public List<Validacion> getValidaciones() {
		
		validaciones = new ArrayList<Validacion>();
		
		try {
			validaciones = validacionEJB.consultarValidaciones();
		} catch(Exception e) {
			addErrorMessage(e,MSG_ID);
		}
		
		return validaciones;
	}


	public void setValidaciones(List<Validacion> validaciones) {
		this.validaciones = validaciones;
	}

	public HtmlDataTable getValidacionesTable() {
		return validacionesTable;
	}

	public void setValidacionesTable(HtmlDataTable validacionesTable) {
		this.validacionesTable = validacionesTable;
	}

	public void eliminarValidacion(ActionEvent event) {
		Validacion validacion = (Validacion)getValidacionesTable().getRowData();
		try {
			validacionEJB.eliminarValidacion(validacion);
			validacionMantenimientoSessionMBean.setModoCreacion(false);
			validacionMantenimientoSessionMBean.setModoEdicion(false);
			validacionMantenimientoSessionMBean.setValidacion(null);
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

	public void editar(ActionEvent event) {

		Validacion v = (Validacion)getValidacionesTable().getRowData();
		
		validacionMantenimientoSessionMBean.setModoEdicion(true);
		validacionMantenimientoSessionMBean.setModoCreacion(false);
		
		List<ParametroValidacion> parametros = new ArrayList<ParametroValidacion>();
		try {
			parametros = validacionEJB.consultarParametrosDeLaValidacion(v);
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}

		validacionMantenimientoSessionMBean.setValidacion(v);
		validacionMantenimientoSessionMBean.getValidacion().setParametrosValidacion(parametros);
		
	}
	
	public void guardarEdicion(ActionEvent event) {

		try {
			
			//Modifica la validacion y sus parametros
			validacionEJB.modificarValidacion(validacionMantenimientoSessionMBean.getValidacion());
			
			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			validacionMantenimientoSessionMBean.setModoEdicion(false);
			validacionMantenimientoSessionMBean.setValidacion(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarEdicion(ActionEvent e) {
		validacionMantenimientoSessionMBean.setModoEdicion(false);
		validacionMantenimientoSessionMBean.setValidacion(null);
	}

	public void crear(ActionEvent event) {
		
		Validacion v = new Validacion();
		
		validacionMantenimientoSessionMBean.setModoEdicion(false);
		validacionMantenimientoSessionMBean.setModoCreacion(true);
		
		validacionMantenimientoSessionMBean.setValidacion(v);
	}

	public void guardarCreacion(ActionEvent event) {

		try {
			
			//Crea la validacion y sus parametros
			validacionEJB.crearValidacion(validacionMantenimientoSessionMBean.getValidacion());

			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			validacionMantenimientoSessionMBean.setModoCreacion(false);
			validacionMantenimientoSessionMBean.setValidacion(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarCreacion(ActionEvent e) {
		validacionMantenimientoSessionMBean.setModoCreacion(false);
		validacionMantenimientoSessionMBean.setValidacion(null);
	}
	
	
	public void crearParametro(ActionEvent event) {
		validacionMantenimientoSessionMBean.getValidacion().getParametrosValidacion().add(new ParametroValidacion());
		int size = validacionMantenimientoSessionMBean.getValidacion().getParametrosValidacion().size();
		validacionMantenimientoSessionMBean.setParametrosTablePageIndex(Double.valueOf(size / parametrosTable.getRows()).intValue()+1);
		parametrosDataScroller.setPage(Double.valueOf(size / parametrosTable.getRows()).intValue()+1);
	}
	
	public void eliminarParametro(ActionEvent event) {
		
		int i = getParametrosTable().getRowIndex();
		validacionMantenimientoSessionMBean.getValidacion().getParametrosValidacion().remove(i);
	}
}
