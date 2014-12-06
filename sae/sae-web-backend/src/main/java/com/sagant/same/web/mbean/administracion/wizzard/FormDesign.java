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

package com.sagant.same.web.mbean.administracion.wizzard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.richfaces.component.html.HtmlDataTable;
import org.richfaces.component.html.HtmlSubTable;

import com.sagant.same.i18n.CurrentLocaleMBean;

import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.web.common.FormularioDinamicoReserva;

public class FormDesign {

	private UIComponent content;
	
	private FormularioDinamicoReserva formBuilder;
	
	List<AgrupacionDato> sectionList;
	
	//HtmlDataTable sectionListTable;
	//List<HtmlSubTable> sectionListSubTables;
	
	//Este mapa se utiliza para almacenar los datos que ingrese el usuario
	//si bien en la vista diseño no hay interación, en un futuro se puede usar para probar validaciones
	private Map<String, Object> data;

	private CurrentLocaleMBean currentLocale;
	
	public FormDesign(String managedBeanContainerName, CurrentLocaleMBean currentLocale) {
	
		this.currentLocale = currentLocale;
		
		formBuilder = new FormularioDinamicoReserva(managedBeanContainerName+".data", managedBeanContainerName.replace(".", "_"), FormularioDinamicoReserva.TipoFormulario.EDICION);
		
		sectionList = new ArrayList<AgrupacionDato>();

		data = new HashMap<String, Object>();
		
		//sectionListSubTables = new ArrayList<HtmlSubTable>();

		//Datos de ejemplo
		buildSection(sectionList, 0, "Datos personales");
		buildSection(sectionList, 1, "Datos del trámite");
		buildField(sectionList.get(0), 0, "Nombre", Tipo.STRING, 20, true);
		buildField(sectionList.get(0), 1, "Apellido", Tipo.STRING, 20, true);
		buildField(sectionList.get(1), 2, "Observaciones", Tipo.STRING, 30, false);
		buildField(sectionList.get(1), 3, "Número de cuenta", Tipo.NUMBER, 10, false);
		
	}
	
	public UIComponent getContent() {
		return content;
	}
	
	public void setContent(UIComponent content) {
		this.content = content;
		if (content.getChildCount() == 0) {
			updateContent();
		}
	}

	
	public List<AgrupacionDato> getSectionList() {
		return sectionList;
	}
	
	public Map<String, Object> getData() {
		return data;
	}
	
	/*public HtmlDataTable getSectionListTable() {
		return sectionListTable;
	}

	public void setSectionListTable(HtmlDataTable sectionListTable) {
		this.sectionListTable = sectionListTable;
	}*/

	/*public HtmlSubTable getSectionListSubTable(){
		int index = sectionListTable.getRowIndex();
		if (index < 0 || index >= sectionListSubTables.size()) {
			return null;
		}
		else {
			return sectionListSubTables.get(sectionListTable.getRowIndex());
		}
	}*/

	/*public void setSectionListSubTable(HtmlSubTable subTable){
		sectionListSubTables.add(sectionListTable.getRowIndex(), subTable);
		
	}*/
	
	public void deleteSection(AgrupacionDato section) {
		//int sectionIndex = sectionListTable.getRowIndex();
		//int sectionIndex = sectionList.indexOf(section);
		sectionList.remove(section);
		//sectionListSubTables.remove(sectionIndex);
		
		updateSectionsOrderInfo(sectionList);
		
		updateContent();
	}

	/*
	 * Agrega una seccion al final de la lista de secciones
	 */
	public void addSection(ActionEvent event) {

		int sectionIndex = sectionList.size();

		buildDefaultSection(sectionList, sectionIndex);
		//Dejo el espacio para que luego el setSectionListSubTable sea llamado y no pise otra HtmlSubTable
		//sectionListSubTables.add(sectionIndex,null);

		updateContent();
	}

	/*
	 * Agrega una seccion encima de la seccion seleccionada
	 */
	public void addSectionAbove(AgrupacionDato section) {
		int sectionIndex = sectionList.indexOf(section);

		buildDefaultSection(sectionList, sectionIndex);
		//Dejo el espacio para que luego el setSectionListSubTable sea llamado y no pise otra HtmlSubTable
		//sectionListSubTables.add(sectionIndex,null);

		updateContent();
	}
	
	/*
	 * Agrega una seccion debajo de la seccion seleccionada
	 */
	public void addSectionBelow(AgrupacionDato section) {
		int sectionIndex = sectionList.indexOf(section) + 1;

		buildDefaultSection(sectionList, sectionIndex);
		//Dejo el espacio para que luego el setSectionListSubTable sea llamado y no pise otra HtmlSubTable
		//sectionListSubTables.add(sectionIndex,null);

		updateContent();
	}

	/*
	 * Agrega un campo al final de la lista de campos de la secion seleccionada
	 */
	public void addField(AgrupacionDato section) {
		
		buildDefaultField(section, section.getDatosASolicitar().size());
		
		updateContent();
	}


	public void deleteField(DatoASolicitar field) {
		field.getAgrupacionDato().getDatosASolicitar().remove(field);
		
		updateFieldsOrderInfo(field.getAgrupacionDato().getDatosASolicitar());
		
		updateContent();
	}


	/*
	 * Agrega un campo encima del campo seleccionado
	 */
	public void addFieldAbove(DatoASolicitar field) {
		int fieldIndex = field.getAgrupacionDato().getDatosASolicitar().indexOf(field);

		buildDefaultField(field.getAgrupacionDato(), fieldIndex);
		
		updateContent();
	}
	
	/*
	 * Agrega un campo inmediatametne debajo del campo seleccionado
	 */
	public void addFieldBelow(DatoASolicitar field) {
		int fieldIndex = field.getAgrupacionDato().getDatosASolicitar().indexOf(field) + 1;

		buildDefaultField(field.getAgrupacionDato(), fieldIndex);
		
		updateContent();
	}

	
	public List<SelectItem> getDataTypeItems() {

		List<SelectItem> items = new ArrayList<SelectItem>();
		for (Tipo t : Tipo.values()) {
			items.add(new SelectItem(t, t.getDescripcion(currentLocale.getLocale())));
		}
		
		return items;
	}

	public void update(ActionEvent event) {
		updateContent();
	}
	
	private void updateContent() {
		
		content.getChildren().clear();
		formBuilder.armarFormulario(sectionList, null);	
		UIComponent uiForm = formBuilder.getComponenteFormulario();
		content.getChildren().add(uiForm);
		
	}

	private void buildDefaultSection(List<AgrupacionDato> sections, int index){
			buildSection(sections, index, currentLocale.getText("wizzard.form.conf.default.section.title"));
	}
	
	private void buildSection(List<AgrupacionDato> sections, int index, String name){

		AgrupacionDato s = new AgrupacionDato();
		s.setNombre(name);
		index = (index > sections.size() ? sections.size() : (index < 0 ? 0 : index));
		s.setOrden(index + 1);
		s.setId(index + 1);
		
		sections.add(index, s);
		
		//Si se inserto la seccion en medio de la lista, debo actualizar la info de orden de cada elemento
		if (index < sections.size()-1) {
			updateSectionsOrderInfo(sections);
		}
	}

	private void buildDefaultField(AgrupacionDato section, int index) {
		buildField(section, index, currentLocale.getText("wizzard.form.conf.default.field.label"), Tipo.STRING, 20, true);
	}
	
	private void buildField(AgrupacionDato section, int index, String label, Tipo type, Integer length, Boolean required) {

		List<DatoASolicitar> fields = section.getDatosASolicitar();
		index = (index > fields.size() ? fields.size() : (index < 0 ? 0 : index));
		
		DatoASolicitar field = new DatoASolicitar();
		field.setEtiqueta(label);
		field.setAgrupacionDato(section); 
		fields.add(index, field);
		field.setFila(index + 1);
		field.setColumna(1);
		field.setTipo(type);
		field.setLargo(length);
		field.setRequerido(required);

		field.setEsClave(false);
		field.setIncluirEnLlamador(false);
		field.setOrdenEnLlamador(index + 1);
		field.setIncluirEnReporte(true);
		field.setAnchoDespliegue(length * 5);
		field.setLargoEnLlamador(length);
		field.setTextoAyuda(null);

		//Al final armo el nombre que debe ser único como composición del orden de la sección, fila y columna.
		//La unicidad me lo garantiza el hecho de que, en este caso, esos valores son definidos por construcción.
		field.setId(index + 1);
		field.setNombre(buildUniqueFieldName(field));
		
		//Si se inserto el campo en medio de la lista, debo actualizar la info de orden de cada elemento
		if (index < fields.size()-1) {
			updateFieldsOrderInfo(fields);
		}
		
	}

	private void updateSectionsOrderInfo(List<AgrupacionDato> sections) {
		int i = 0;
		for (AgrupacionDato section : sections) {
			section.setOrden(i+1);
			section.setId(i+1);
			i++;
			updateFieldsOrderInfo(section.getDatosASolicitar());
		}
	}
	
	private void updateFieldsOrderInfo(List<DatoASolicitar> fields) {
		int i = 0;
		for (DatoASolicitar field : fields) {
			field.setFila(i+1);
			field.setId(i+1);
			i++;
			//La unicidad me lo garantiza el hecho de que, en este caso, esos valores son definidos por construcción.
			field.setNombre(buildUniqueFieldName(field));
		}
	}
	
	private String buildUniqueFieldName(DatoASolicitar field){
		return "F"+field.getAgrupacionDato().getId()+"_"+field.getId();
	}
}
