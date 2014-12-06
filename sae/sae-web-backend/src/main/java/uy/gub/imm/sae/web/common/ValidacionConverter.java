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

package uy.gub.imm.sae.web.common;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import uy.gub.imm.sae.entity.Validacion;
import uy.gub.imm.sae.web.mbean.administracion.ValidacionAsignacionSessionMBean;

public class ValidacionConverter implements Converter {
	public Object getAsObject(FacesContext ctx, UIComponent arg1, String arg2) {

		ELContext elContext = ctx.getELContext();
		ExpressionFactory expFactory = ctx.getApplication().getExpressionFactory();
		String el = "#{validacionAsignacionSessionMBean}"; 
		ValueExpression ve = expFactory.createValueExpression(elContext, el, ValidacionAsignacionSessionMBean.class);
		ValidacionAsignacionSessionMBean sesion = (ValidacionAsignacionSessionMBean) ve.getValue(elContext);
		
		for (Validacion validacion : sesion.getValidaciones()) {
			if (validacion.getId().equals(Integer.valueOf(arg2))) {
				return validacion;		
			}
		}
		
		
		return null;

		
/*			Asociacion a = null;
		
		if (arg2 != null) {
			
			JSONMap json;
			try {
				json = new JSONMap(arg2);
				a = new Asociacion();
				a.setNombreCampo((String)json.get("campo"));
				a.setNombreParametro((String)json.get("parametro"));
				a.setValidacionPorRecursoId((Integer)json.get("validacionId"));
				a.setDatoASolicitarId((Integer)json.get("campoId"));
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		*/
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {

		Validacion validacion = (Validacion) arg2;
		return validacion.getId().toString();

		/*String j = null;
		
		if (arg2 != null) {
			Asociacion a = (Asociacion) arg2;
			StringWriter w = new StringWriter();
			JSONWriter json = new JSONWriter(w);
			try {
				json.object()
						.key("campo")
						.value(a.getNombreCampo())
						.key("parametro")
						.value(a.getNombreParametro())
						.key("campoId")
						.value(a.getDatoASolicitarId())
						.key("validacionId")
						.value(a.getValidacionPorRecursoId())
					.endObject();
			
				w.flush();
				j = w.toString();
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		*/
	}
}
	
