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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputFormat;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;

import org.ajax4jsf.component.html.HtmlAjaxCommandButton;
import org.richfaces.component.html.HtmlCalendar;
import org.richfaces.component.html.HtmlPanel;
import org.richfaces.component.html.HtmlRichMessage;
import org.richfaces.component.html.HtmlSpacer;

import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.ValorPosible;

/**
 * Clase utilitaria, tiene la logica para generar componentes jsf y richfaces en runtime
 * para desplegar el formulario de ingreso de datos de la reserva en forma dinamica 
 * según el modelo de datos definido.
 * @author im2716295
 *
 */
public class FormularioDinamicoReserva {
	// Contiene el valor de la lista con significado "No filtrar por este valor"
	private static final String VALOR_LISTA_NO_SELECCION = "NoSeleccion";

	public enum TipoFormulario {LECTURA, EDICION, EDICION_CONSULTA};
	
	//CSS: Clases utilizadas para dar estilo a los componentes del formulario
	private static final String STYLE_CLASS_MENSAJE_ERROR = "mensajeError";
	private static final String STYLE_CLASS_MENSAJE_INFO = "mensajeInfo";
	private static final String STYLE_CLASS_CAMPO_CON_ERROR = "formularioCampoConError";
	private static final String STYLE_CLASS_CAMPO_SIN_ERROR = "formularioCampoSinError";

	private static final String STYLE_CLASS_FORMULARIO = "formulario";
	private static final String STYLE_CLASS_CABEZAL = "formularioCabezal";
	private static final String STYLE_CLASS_CUERPO = "formularioCuerpo";
	private static final String STYLE_CLASS_ETIQUETA = "formularioEtiqueta";
	private static final String STYLE_CLASS_ETIQUETA_EDICION = "formularioEtiquetaAlineacionEdicion";
	private static final String STYLE_CLASS_ETIQUETA_LECTURA = "formularioEtiquetaAlineacionLectura";
	private static final String STYLE_CLASS_ETIQUETA_ANCHO = "formularioEtiquetaAncho";
	private static final String STYLE_CLASS_TEXTO_ETIQUETA = "pasoTexto";
	 
	private static final String STYLE_CLASS_CAMPO = "formularioCampo";
	private static final String STYLE_CLASS_CAMPO_EDICION = "formularioCampoAlineacionEdicion";
	private static final String STYLE_CLASS_CAMPO_LECTURA = "formularioCampoAlineacionLectura";
	private static final String STYLE_CLASS_TEXTO_CAMPO = "pasoTexto";
	private static final String STYLE_CLASS_REQUERIDO = "formularioCampoRequerido";

	//Bandera que inidca si el formulario debe ser de solo lectura.
	private boolean soloLectura;
	private TipoFormulario tipoFormulario;
	
	//En caso de formulario de edicion
	private String managedBean;
	private String nombreFormulario;
	
	//En caso de formulario de solo lectura
	private Map<String, Object> valores;
	
	private Application app;

	private HtmlPanelGrid formularioGrilla;
	private HtmlPanelGrid mensajesGrilla;
	
	/*
	 * Crea un formulario editable de un tipo (consulta o edicion)
	 */	
	public FormularioDinamicoReserva(String managedBeanName, String nombreFormulario, TipoFormulario tipo) {
		this.managedBean = managedBeanName;
		this.nombreFormulario = nombreFormulario;
		this.app = FacesContext.getCurrentInstance().getApplication();
	
		this.tipoFormulario = tipo;
		this.soloLectura = (tipo == TipoFormulario.LECTURA);
	}
	
	/*
	 * Crea un formulario editable
	 */
/*	public FormularioDinamicoReserva(String managedBeanName, String nombreFormulario) {

		this.managedBean = managedBeanName;
		this.nombreFormulario = nombreFormulario;
		this.app = FacesContext.getCurrentInstance().getApplication();
	
		this.tipoFormulario = TipoFormulario.EDICION;
		this.soloLectura = false;
	}
*/
	
	/*
	 * Crea un formulario de solo lectura
	 */
	public FormularioDinamicoReserva(Map<String, Object> valores) {
		
		this.valores = new HashMap<String, Object>(valores);
		this.app = FacesContext.getCurrentInstance().getApplication();

		this.tipoFormulario = TipoFormulario.LECTURA;
		this.soloLectura = true;
	}

	public static void marcarCamposError(List<String> idComponentes, UIComponent formulario) {
		for (String id : idComponentes) {
			UIComponent comp = formulario.findComponent(id);
			if (comp instanceof HtmlInputText) {
				HtmlInputText input = (HtmlInputText) comp;
				input.setStyleClass(STYLE_CLASS_CAMPO_CON_ERROR);
			}
			if (comp instanceof HtmlSelectOneMenu) {
				HtmlSelectOneMenu input = (HtmlSelectOneMenu) comp;
				input.setStyleClass(STYLE_CLASS_CAMPO_CON_ERROR);
			}
			if (comp instanceof HtmlCalendar) {
				HtmlCalendar input = (HtmlCalendar) comp;
				input.setInputClass(STYLE_CLASS_CAMPO_CON_ERROR);
			}
		}
	}

	public static void desmarcarCampos(List<String> idComponentes, UIComponent formulario) {
		for (String id : idComponentes) {
			UIComponent comp = formulario.findComponent(id);
			if (comp instanceof HtmlInputText) {
				HtmlInputText input = (HtmlInputText) comp;
				input.setStyleClass("");
			}
			if (comp instanceof HtmlSelectOneMenu) {
				HtmlSelectOneMenu input = (HtmlSelectOneMenu) comp;
				input.setStyleClass("");
			}
			if (comp instanceof HtmlCalendar) {
				HtmlCalendar input = (HtmlCalendar) comp;
				input.setInputClass("");
			}
		}
	}
	
	public void armarFormulario(List<AgrupacionDato> agrupaciones, HashMap<Integer,HashMap<Integer,ServicioPorRecurso>> serviciosAutocompletar) {

		if (!(tipoFormulario == TipoFormulario.LECTURA)) {
			//Seccion de mensajes de error
			mensajesGrilla = (HtmlPanelGrid) app.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
			mensajesGrilla.setId("msgGrilla");
			mensajesGrilla.setCellpadding("0px");
			mensajesGrilla.setCellspacing("0px");
			mensajesGrilla.setColumns(1);
			mensajesGrilla.setWidth("100%");
			//Mensaje generico
			mensajesGrilla.getChildren().add(armarMensajeValidacion(nombreFormulario));
		}
		
		formularioGrilla = (HtmlPanelGrid) app.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
		formularioGrilla.setId(nombreFormulario);
		formularioGrilla.setColumns(1);
		formularioGrilla.setWidth("100%");
		
/*		if (!soloLectura) {
			formularioGrilla.getChildren().add(mensajes);
		}
*/	
		formularioGrilla.setCellspacing("0");
		formularioGrilla.setCellpadding("0");

		for (AgrupacionDato agrupacionDato : agrupaciones) {
			UIComponent agrupacion = armarAgrupacion(agrupacionDato, serviciosAutocompletar);
			formularioGrilla.getChildren().add(agrupacion);
		}
		
	}
	
	private UIComponent armarAgrupacion(AgrupacionDato agrupacionDato, HashMap<Integer,HashMap<Integer,ServicioPorRecurso>> serviciosAutocompletar) {
		
		//HtmlPanel panel =  (HtmlPanel) app.createComponent(HtmlPanel.COMPONENT_TYPE);
		
		HtmlPanel panel =  new HtmlPanel();
		panel.setStyleClass(STYLE_CLASS_FORMULARIO);
		panel.setHeader(agrupacionDato.getNombre());
		panel.setHeaderClass(STYLE_CLASS_CABEZAL);
		panel.setBodyClass(STYLE_CLASS_CUERPO);
		HtmlPanelGrid grid = (HtmlPanelGrid) app.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
		grid.setColumns(2);
		grid.setCellspacing("0");
		grid.setCellpadding("0");
		grid.setId("panel-grid-agr" + agrupacionDato.getId());
		
		if (tipoFormulario == TipoFormulario.LECTURA) {
			grid.setColumnClasses(
					STYLE_CLASS_ETIQUETA + " " + STYLE_CLASS_ETIQUETA_LECTURA + " " + STYLE_CLASS_ETIQUETA_ANCHO + ", " + 
					STYLE_CLASS_CAMPO + " " + STYLE_CLASS_CAMPO_LECTURA);
		}
		else {
			grid.setColumnClasses(
					STYLE_CLASS_ETIQUETA + " " + STYLE_CLASS_ETIQUETA_EDICION  + " " + STYLE_CLASS_ETIQUETA_ANCHO + ", " + 
					STYLE_CLASS_CAMPO + " " + STYLE_CLASS_CAMPO_EDICION);
		}
		panel.getChildren().add(grid);
		
		//Los datos deberían estar ordenados por fila,columna.
		//y la numeracion comienza en (1,1)
		List<DatoASolicitar> datos = new ArrayList<DatoASolicitar>(agrupacionDato.getDatosASolicitar());

		//Son la fila y columna del grid que agrupo los campos asia abajo, es n x 2 donde n = cantidad de filas de los datos.
		Integer filaActual = null;
		Boolean primerColumna = null;

		for (DatoASolicitar datoASolicitar : datos) {

			if ( ! datoASolicitar.getFila().equals(filaActual) ) {
				filaActual = datoASolicitar.getFila();
				primerColumna = true;
			}
			
			UIComponent campo [] = armarCampo(datoASolicitar, primerColumna);
			
			if (primerColumna) {
				HtmlPanelGroup inputs = new HtmlPanelGroup();
				grid.getChildren().add(campo[0]); //Etiqueta
				grid.getChildren().add(inputs);   //agrupo las entradas para el caso de campos relacionados ej: dir, apto, bloque.
				inputs.getChildren().add(campo[1]); //Input
				
				if (tipoFormulario == TipoFormulario.EDICION){
					
					if(campo[2] != null){
						HtmlSpacer espacio = new HtmlSpacer();
						espacio.setWidth("3px");
						
						inputs.getChildren().add(espacio);
						inputs.getChildren().add(campo[2]); //Ayuda
					}
					
					if (serviciosAutocompletar != null){
						
						HashMap<Integer,ServicioPorRecurso> servicios = serviciosAutocompletar.get(datoASolicitar.getId());
						if (servicios != null){
							
							//FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(servicio.getId().toString(), servicio);
							String claves = "";
							for (Integer keyServicio : servicios.keySet()){
								claves += servicios.get(keyServicio).getId().toString() + "|";
							}
							
							UIParameter parameter = new UIParameter();
							parameter.setName("paramIdsServicio");
							parameter.setValue(claves);
							
							HtmlAjaxCommandButton btn = new HtmlAjaxCommandButton();
							MethodExpression me = app.getExpressionFactory().createMethodExpression(FacesContext.getCurrentInstance().getELContext(),"#{paso3MBean.autocompletarCampo}",String.class, new Class<?>[0]);
							btn.setActionExpression(me);
							btn.setValue("completar");
							btn.setStyleClass("arriba");
							btn.setReRender("formulario");
							
							btn.getChildren().add(parameter);
							
							
							HtmlSpacer espacio = new HtmlSpacer();
							espacio.setWidth("3px");
							
							inputs.getChildren().add(espacio);
							inputs.getChildren().add(btn); //Boton
						}
					}
				}
				primerColumna = false;
			}
			else {
				HtmlPanelGroup inputs = (HtmlPanelGroup) grid.getChildren().get(grid.getChildCount()-1);
				
				HtmlSpacer espacio1 = new HtmlSpacer();
				espacio1.setWidth("3px");
				HtmlSpacer espacio2 = new HtmlSpacer();
				espacio2.setWidth("3px");
				
				inputs.getChildren().add(espacio1);
				inputs.getChildren().add(campo[0]); //Etiqueta
				inputs.getChildren().add(espacio2);
				inputs.getChildren().add(campo[1]); //Input

				if (tipoFormulario == TipoFormulario.EDICION){
					
					if (campo[2] != null){
						HtmlSpacer espacio3 = new HtmlSpacer();
						espacio3.setWidth("3px");
						
						inputs.getChildren().add(espacio3);
						inputs.getChildren().add(campo[2]); //Ayuda
					}
					
					if (serviciosAutocompletar != null){
						
						HashMap<Integer,ServicioPorRecurso> servicios = serviciosAutocompletar.get(datoASolicitar.getId());
						if (servicios != null){
							
							//FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(servicio.getId().toString(), servicio);
							String claves = "";
							for (Integer keyServicio : servicios.keySet()){
								claves += servicios.get(keyServicio).getId().toString() + "|";
							}
							
							UIParameter parameter = new UIParameter();
							parameter.setName("paramIdsServicio");
							parameter.setValue(claves);
							
							HtmlAjaxCommandButton btn = new HtmlAjaxCommandButton();
							MethodExpression me = app.getExpressionFactory().createMethodExpression(FacesContext.getCurrentInstance().getELContext(),"#{paso3MBean.autocompletarCampo}",String.class, new Class<?>[0]);
							btn.setActionExpression(me);
							btn.setValue("completar");
							btn.setStyleClass("arriba");
							btn.setReRender("formulario");
							
							btn.getChildren().add(parameter);
							
							
							HtmlSpacer espacio = new HtmlSpacer();
							espacio.setWidth("3px");
							
							inputs.getChildren().add(espacio);
							inputs.getChildren().add(btn); //Boton
						}
					}
				}
			}
		}
		
		return panel;
	}
	
	public UIComponent getComponenteFormulario() {
		return formularioGrilla;
	}

	public UIComponent getComponenteMensajes() {
		return mensajesGrilla;
	}

	/**
	 * Un campo consiste de una etiqueta y un campo editable, o sea un input.
	 */
	private UIComponent [] armarCampo(DatoASolicitar dato, Boolean primerColumna) {
		
		String requerido = "";
		if (dato.getRequerido() && ! soloLectura && ! (tipoFormulario == TipoFormulario.EDICION_CONSULTA)
			&& primerColumna) {
			requerido = "<span class=\""+STYLE_CLASS_REQUERIDO+"\"> * </span>";
		}

		String dosPuntos = "";
		String tablaInicio = "";
		String tablaFin = "";
		if (primerColumna) {
			dosPuntos = ":";
			
			if (tipoFormulario == TipoFormulario.LECTURA) {
				tablaInicio = "<table cellpadding='0' cellspacing='0' border='0' align='left'><tr><td nowrap='nowrap' style='text-align: left;'>";
			}
			else {
				tablaInicio = "<table cellpadding='0' cellspacing='0' border='0' align='right'><tr><td nowrap='nowrap' style='text-align: right;'>";
			}
			tablaFin = "</td></tr></table>";
		}
		
		
		HtmlOutputFormat etiqueta = new HtmlOutputFormat();
		etiqueta.setValue(tablaInicio + requerido + dato.getEtiqueta() + dosPuntos + tablaFin);
		etiqueta.setEscape(false);
		etiqueta.setStyleClass(STYLE_CLASS_TEXTO_ETIQUETA);
		
		UIComponent input;
		
		switch (dato.getTipo()) {
		case STRING:
			input = armarCampoString(dato);
			break;
		case DATE:
			input = armarCampoDate(dato);
			break;
		case LIST:
			input = armarCampoList(dato);
			break;
		case BOOLEAN:
			input = armarCampoBoolean(dato);
			break;
		default:
			input = armarCampoString(dato);
			break;
		}
		
		input.setId(dato.getNombre());

		HtmlOutputFormat imgAyuda = new HtmlOutputFormat();
		imgAyuda.setValue("<img src='"+SAEProfile.getInstance().getProperties().getProperty(SAEProfile.PROFILE_UI_RESOURCE_BASE_URL_KEY)+"/recursos/images/info.png' TITLE='" + dato.getTextoAyuda() + "'/>");
		imgAyuda.setEscape(false);
		//imgAyuda.setStyleClass(STYLE_CLASS_TEXTO_ETIQUETA);
		
		UIComponent campo [] = new UIComponent[3];
		campo[0] = etiqueta;
		campo[1] = input;
		
		if (dato.getTextoAyuda() != null && ! dato.getTextoAyuda().equals("")) {
			campo[2] = imgAyuda;
		}
		
		if (!soloLectura) {
			mensajesGrilla.getChildren().add(armarMensajeValidacion(dato.getNombre()));
		}
			
		return campo;
	}
	
	/**
	 * Un campo de tipo string consiste en un campo editable.
	 */
	private UIComponent armarCampoString (DatoASolicitar dato) {

		UIComponent campo = null;

		if (soloLectura) {
			HtmlOutputText output = (HtmlOutputText) app.createComponent(HtmlOutputText.COMPONENT_TYPE);
			output.setValue(this.valores.get(dato.getNombre()));
			output.setStyleClass(STYLE_CLASS_TEXTO_CAMPO);
			campo = output;
		}
		else {
			HtmlInputText input = (HtmlInputText) app.createComponent(HtmlInputText.COMPONENT_TYPE);
			input.setMaxlength(dato.getLargo());
			input.setSize(dato.getLargo());
			input.setStyleClass(STYLE_CLASS_TEXTO_CAMPO);

			//Le configuro le managed bean donde debe almacenar el valor que ingrese el usuario.
			ValueExpression ve = armarExpresion(dato.getNombre(), String.class);
			input.setValueExpression("value", ve);
			
			campo = input;
		}
		
		return campo;
	}
	
	/**
	 * Un campo de tipo date consiste en un calendario
	 */
	private UIComponent armarCampoDate (DatoASolicitar dato) {

		UIComponent campo = null;
		
		if (soloLectura) {
			HtmlOutputText output = (HtmlOutputText) app.createComponent(HtmlOutputText.COMPONENT_TYPE);

			if (this.valores.get(dato.getNombre()) != null) {
				//28/05/2014 - ANGARCIA
	            Date dFecha;
	            String sFecha="";           
				try {
					SimpleDateFormat parserFecha = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
					dFecha = parserFecha.parse((String) this.valores.get(dato.getNombre()));
					parserFecha = new SimpleDateFormat("dd/MM/yyyy");
					sFecha = parserFecha.format(dFecha);
				} catch (ParseException ex) {
					sFecha = (String) this.valores.get(dato.getNombre());
				}
				output.setValue(sFecha);
				// 28/05/2014 - FIN ANGARCIA			
			}
			output.setStyleClass(STYLE_CLASS_TEXTO_CAMPO);

			campo = output;
		}
		else {
			HtmlCalendar calendario = (HtmlCalendar) app.createComponent(HtmlCalendar.COMPONENT_TYPE);
			calendario.setStyleClass(STYLE_CLASS_CAMPO_SIN_ERROR);
			calendario.setEnableManualInput(false);
			calendario.setPopup(true);
			calendario.setInputSize(dato.getLargo());
			calendario.setInputClass(STYLE_CLASS_TEXTO_CAMPO);

			//Le configuro el managed bean donde debe almacenar el valor que ingrese el usuario.
			ValueExpression ve = armarExpresion(dato.getNombre(), Date.class);
			calendario.setValueExpression("value", ve);
			
			//27/05/2014 - ANGARCIA
			calendario.setDatePattern("dd/MM/yyyy");
			//27/05/2014 - FIN ANGARCIA

			campo = calendario;
		}
		
		return campo;
	}
	
	
	/**
	 * Un campo de tipo List consiste de una etiqueta y una lista de valores desplegable
	 */
	private UIComponent armarCampoList (DatoASolicitar dato) {

		UIComponent campo = null;
		
		if (soloLectura) {
			HtmlOutputText output = (HtmlOutputText) app.createComponent(HtmlOutputText.COMPONENT_TYPE);
			output.setStyleClass(STYLE_CLASS_TEXTO_CAMPO);
			
			//Busco la etiqueta del valor posible:
			Iterator<ValorPosible> iter = dato.getValoresPosibles().iterator();
			String etiqueta = null;
			while (iter.hasNext() && etiqueta == null) {
				ValorPosible vp = iter.next();
				if (vp.getValor().equals(this.valores.get(dato.getNombre()))) {
					etiqueta = vp.getEtiqueta();
				}
			}
			if (etiqueta != null) {
				output.setValue(etiqueta);
			}
			else {
				//Por precaución, aunque siempre debería poder obtener la etiqueta del valor.
				output.setValue(this.valores.get(dato.getNombre()));
			}

			campo = output;
		}
		else {
			HtmlSelectOneMenu lista = (HtmlSelectOneMenu) app.createComponent(HtmlSelectOneMenu.COMPONENT_TYPE);
			lista.setStyleClass(STYLE_CLASS_TEXTO_CAMPO + " " + STYLE_CLASS_CAMPO_SIN_ERROR);
	
			List<UISelectItem> items = armarListaDeValores(dato);
			for (UISelectItem item: items) {
				lista.getChildren().add(item);
			}

			//Le configuro le managed bean donde debe almacenar el valor que ingrese el usuario.
			ValueExpression ve = armarExpresion(dato.getNombre(), String.class);
			lista.setValueExpression("value", ve);
			
			campo = lista;
		}
			
		return campo;
	}
	

	/**
	 * Una lista de SelectItem done cada elemento tiene la etiqueta y el valor que se mostrara en la lista desplegable.
	 */
	private List<UISelectItem> armarListaDeValores (DatoASolicitar dato) {
		
		List<UISelectItem> items = new ArrayList<UISelectItem>();
		
		/* En el caso de la consulta dinamica se agrega un valor a la 
		 * lista igual a "No filtrar por ese campo"
		 */
		if (tipoFormulario ==TipoFormulario.EDICION_CONSULTA){
			UISelectItem item = new UISelectItem();
			item.setItemLabel(" ");
			item.setItemValue(VALOR_LISTA_NO_SELECCION);
			items.add(item);
		}
		
		// Agrego el resto de las opciones
		for (ValorPosible valor: dato.getValoresPosibles()) {
			UISelectItem item = new UISelectItem();
			item.setItemLabel(valor.getEtiqueta());
			item.setItemValue(valor.getValor());
			items.add(item);
		}

		
		
		return items;
	}
	
	/**
	 * Un campo de tipo boolean consiste en un campo editable.
	 */
	private UIComponent armarCampoBoolean (DatoASolicitar dato) {

		UIComponent campo = null;
		
		if (soloLectura) {
			HtmlSelectBooleanCheckbox output = (HtmlSelectBooleanCheckbox) app.createComponent(HtmlSelectBooleanCheckbox.COMPONENT_TYPE);
			output.setValue(Boolean.valueOf(this.valores.get(dato.getNombre()).toString()));
			output.setStyleClass(STYLE_CLASS_TEXTO_CAMPO);
			output.setDisabled(true);
			campo = output;
		}
		else {
			HtmlSelectBooleanCheckbox input = (HtmlSelectBooleanCheckbox) app.createComponent(HtmlSelectBooleanCheckbox.COMPONENT_TYPE);			
			input.setStyleClass(STYLE_CLASS_TEXTO_CAMPO);

			//Le configuro le managed bean donde debe almacenar el valor que ingrese el usuario.
			ValueExpression ve = armarExpresion(dato.getNombre(), Boolean.class);
			input.setValueExpression("value", ve);
			
			campo = input;
		}
		
		return campo;
	}

	
	private ValueExpression armarExpresion(String nombre, Class<?> clazz) { 
	
		//Armo la EL que liga el valor del campo editable a un managed bean generico (Map) que recolectará los datos del formulario
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		ExpressionFactory expFactory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
		//TODO sacar el managed bean a atributo de la clase.
		String el = "#{"+managedBean+"."+nombre+"}"; 
		ValueExpression ve = expFactory.createValueExpression(elContext, el, clazz);

		return ve;
	}

	private UIComponent armarMensajeValidacion(String para) {
	
		HtmlRichMessage mensaje = (HtmlRichMessage) app.createComponent(HtmlRichMessage.COMPONENT_TYPE);
		//UIMessage mensaje = (UIMessage) app.createComponent(UIMessage.COMPONENT_TYPE);
		mensaje.setAjaxRendered(true);
		mensaje.setFor(para);
		mensaje.setId("mensaje"+para);
		mensaje.setErrorClass(STYLE_CLASS_MENSAJE_ERROR);
		mensaje.setInfoClass(STYLE_CLASS_MENSAJE_INFO);
		
		return mensaje;
	}
	
	
	public static Map<String, DatoASolicitar> obtenerCampos(List<AgrupacionDato> agrupaciones) {
		
		Map<String, DatoASolicitar> camposXnombre = new HashMap<String, DatoASolicitar>();
		
		for (AgrupacionDato agrupacion : agrupaciones) {
			for (DatoASolicitar dato : agrupacion.getDatosASolicitar()) {
				camposXnombre.put(dato.getNombre(), dato);
			}
		}
		
		return camposXnombre;
	}

}
