package uy.gub.imm.sae.web.common;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import uy.gub.imm.sae.entity.Accion;
import uy.gub.imm.sae.web.mbean.administracion.AccionAsignacionSessionMBean;

public class AccionConverter implements Converter {
	public Object getAsObject(FacesContext ctx, UIComponent arg1, String arg2) {

		ELContext elContext = ctx.getELContext();
		ExpressionFactory expFactory = ctx.getApplication().getExpressionFactory();
		String el = "#{accionAsignacionSessionMBean}"; 
		ValueExpression ve = expFactory.createValueExpression(elContext, el, AccionAsignacionSessionMBean.class);
		AccionAsignacionSessionMBean sesion = (AccionAsignacionSessionMBean) ve.getValue(elContext);
		
		for (Accion accion : sesion.getAcciones()) {
			if (accion.getId().equals(Integer.valueOf(arg2))) {
				return accion;		
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
				a.setAccionPorRecursoId((Integer)json.get("accionId"));
				a.setDatoASolicitarId((Integer)json.get("campoId"));
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		*/
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {

		Accion accion = (Accion) arg2;
		return accion.getId().toString();

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
						.key("accionId")
						.value(a.getAccionPorRecursoId())
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
	

