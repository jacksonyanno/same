/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.
 *
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

package com.sagant.same.web.mbean.administracion;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import uy.gub.imm.sae.business.api.AgendaGeneral;
import uy.gub.imm.sae.business.api.AgendaGeneralRemote;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.mbean.administracion.AgendaMBean;

@ManagedBean
@SessionScoped
public class AgendaSelectionMBean extends BaseMBean implements Serializable {

	private static final long serialVersionUID = 1L;

	
	@EJB(beanInterface = AgendaGeneralRemote.class)
	private AgendaGeneral generalEJB;
	
	private List<Agenda> agendas;
	
	private Recurso selected;
	
	@PostConstruct
	public void init() {
		
		agendas = new ArrayList<Agenda>();		
		selected = null;
		
		refreshAgendasDataModel();
	}
	
	
	public List<Agenda> getAgendas() {
		return agendas;
	}

	public boolean isSelected(Recurso recurso) {
		
		if (recurso == null) {
			return false;
		}
		
		if (selected == null || ! selected.equals(recurso)) {
			return false;
		}
		else {
			return true;
		}
	}


	public Recurso getSelected() {
		return selected;
	}


	public String select(Recurso recurso) {
		
		selected = recurso;

		return null;
	}

	public void refreshAgendasDataModel() {
		
		agendas.clear();

		Boolean notExist = true;
		
		try {
			List<Agenda> agendasDTO = generalEJB.consultarAgendas();
			for (Agenda agenda : agendasDTO) {
				agendas.add(agenda);
				List<Recurso> recursosDeUnaAgenda = generalEJB.consultarRecursos(agenda);
				agenda.setRecursos(recursosDeUnaAgenda);
				
				for (Recurso recurso : recursosDeUnaAgenda) {
					recurso.setAgenda(agenda);
				}
				
				if (selected != null && notExist) {
					notExist = ! recursosDeUnaAgenda.contains(selected);
				}
			}
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		if (notExist) {
			selected = null;
		}
	}

	
	public void groupDescriptionValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		String desc = (String) value;
		
		if (desc == null || desc.isEmpty() || desc.length() > AgendaMBean.GROUP_DESCRIPTION_MAX_SIZE || desc.matches("^\\s+$")) {

			String message = MessageFormat.format(
					getI18N().getText("entity.agenda.message.validation.description_invalid"),
					AgendaMBean.GROUP_DESCRIPTION_MAX_SIZE);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
			addErrorMessage(message, MSG_ID);
			
			throw new ValidatorException(m);
		}

	}
}