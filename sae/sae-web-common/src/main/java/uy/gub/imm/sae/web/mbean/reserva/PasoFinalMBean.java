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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.factories.BusinessLocatorFactory;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.FormularioDinamicoReserva;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
/**
 * Presenta todos los datos de la reserva y da la opción de imprimir un recibo.
 * @author im2716295
 *
 */
public class PasoFinalMBean extends PasoMBean {

	private Recursos recursosEJB;
	

	private List<DatoDelRecurso> infoRecurso;
	
	private UIComponent campos;
	
	private Logger logger = Logger.getLogger(PasoFinalMBean.class);

	
	@PostConstruct
	public void init() {

		try {

			if(getEsIntranet()){
				recursosEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getRecursos();
			} else {
				recursosEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getRecursos();
			}
			
			if (sesionMBean.getAgenda() == null || sesionMBean.getRecurso() == null || sesionMBean.getReservaConfirmada() == null) {
				
				logger.debug("RESERVA: ESTADO INVALIDO PASO FINAL" + "  Agenda: "+sesionMBean.getAgenda()+ " Recurso: "+sesionMBean.getRecurso()+ " ReservaConfirmada: "+sesionMBean.getReservaConfirmada());
				
				redirect(ESTADO_INVALIDO_PAGE_OUTCOME);
				return;
			}
		} catch (ApplicationException e) {
			logger.error("NO SE PUDO OBTENER EJB Recursos");
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
	
	
	public String getEtiquetaDelRecurso() {

		String result = null;
		
		if (sesionMBean.getAgenda().getTextoAgenda() != null) {
			result = sesionMBean.getAgenda().getTextoAgenda().getTextoSelecRecurso();
		}

		return (result == null ? "":result);
	}
	
	public List<DatoDelRecurso> getInfoRecurso() {

		if (infoRecurso == null) {
			if (sesionMBean.getRecurso() != null) {
				try {
					infoRecurso = recursosEJB.consultarDatosDelRecurso(sesionMBean.getRecurso());
					if (infoRecurso.isEmpty()) {
						infoRecurso = null;
					}
				} catch (Exception e) {
					addErrorMessage(e);
				}
			}
		}
		return infoRecurso;
	}	
	
	public UIComponent getCampos() {
		return campos;
	}
	
	public void setCampos(UIComponent campos) {
		
		this.campos = campos;
		
		String mensajeError = "";
		try {
			Recurso recurso = sesionMBean.getRecurso();

			
			//El chequeo de recurso != null es en caso de un acceso directo a la pagina, es solo
			//para que no salte la excepcion en el log, pues de todas formas sera redirigido a una pagina de error.
			if (campos.getChildCount() == 0 && recurso != null) {
				
				
				mensajeError = getI18N().getText("message.set_campos.error_part1");
				
				List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recurso);
				
				Reserva rtmp = sesionMBean.getReservaConfirmada();

				if (rtmp == null) {
					mensajeError += "nulo";
				}
				else {
					mensajeError += rtmp.getId() + ":" + rtmp.getFechaCreacion() + ":";
					
					if (rtmp.getDatosReserva()== null) {
						mensajeError += getI18N().getText("message.set_campos.error_part2");
					}
				
				}
				
				Map<String, Object> valores = obtenerValores(sesionMBean.getReservaConfirmada().getDatosReserva());
				FormularioDinamicoReserva formularioDin = new FormularioDinamicoReserva(valores);
				formularioDin.armarFormulario(agrupaciones, null);
				UIComponent formulario = formularioDin.getComponenteFormulario();
				campos.getChildren().add(formulario);
			}
		} catch (Exception e) {
			logger.error(mensajeError, e);
			redirect(ERROR_PAGE_OUTCOME);
		}
	}

	public String getDescripcion() {
		
		String result = null;
		
		if (sesionMBean.getAgenda().getTextoAgenda() != null) {
			result = sesionMBean.getAgenda().getTextoAgenda().getTextoTicketConf();
		}

		return (result == null ? "":result);
	}
	

	/**
	 * @param datos de alguna reserva
	 * @return retorna los valores de cada dato en un mapa cuya clave es el nombre del campo 
	 */
	private Map<String, Object> obtenerValores(Set<DatoReserva> datos) {
		
		Map<String, Object> valores = new HashMap<String, Object>();
		
		for (DatoReserva dato : datos) {
			//TODO parsear el valor de string a object segun el tipo del DatoASolicitar
			valores.put(dato.getDatoASolicitar().getNombre(), dato.getValor());
		}
		
		return valores;
	}
	
	
	
	public void beforePhase (PhaseEvent event) {
		disableBrowserCache(event);
	}
	
	public String imprimirTicket() {
		try {

			BaseColor colorBlack = new BaseColor(0,0,0);
			
			BaseFont times = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			BaseFont helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			BaseFont symbol = BaseFont.createFont(BaseFont.SYMBOL, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			
			SimpleDateFormat sdfHr = new SimpleDateFormat ("HH:mm");
			SimpleDateFormat sdfFecha = new SimpleDateFormat ("dd/MM/yyyy");
			
			Rectangle pageSize = new Rectangle(210,210);
			
			Document document = new Document(pageSize);
			document.addTitle(getI18N().getText("etiqueta.reserva.title"));
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			PdfWriter pdfWriter = PdfWriter.getInstance(document, os);
			document.open();
			
			PdfContentByte pdfContent = pdfWriter.getDirectContent();
			
			InputStream is = PasoFinalMBean.class.getResourceAsStream(SAEProfile.getInstance().getProperties().getProperty(SAEProfile.PROFILE_UI_TEMPLATES_IMAGES_LOGO_TICKET_KEY));
			
			byte[] arrImage = new byte [4096];
			is.read(arrImage);
			
			Image img = Image.getInstance(arrImage);
			
			img.scaleAbsolute(100,30);
			img.setAbsolutePosition(55, 170);
			document.add(img);

			//Dibujo primer línea
			LineSeparator line = new LineSeparator();
			line.setAlignment(LineSeparator.ALIGN_CENTER);
			line.setLineColor(colorBlack);
			line.setLineWidth(0.5f);
			
			line.drawLine(pdfContent, 10, 200,  170);

			//Etiqueta RESERVA
			pdfContent.beginText();
			pdfContent.setFontAndSize(helveticaBold, 15);
			pdfContent.setTextMatrix(45, 150);
			pdfContent.showText(getI18N().getText("etiqueta.reserva.showText") );
			pdfContent.endText();
			
			//Fecha de la reserva
			String fecha_reserva = sdfFecha.format(sesionMBean.getDisponibilidad().getHoraInicio());
			
			pdfContent.beginText();
			pdfContent.setFontAndSize(symbol, 16);
			pdfContent.setTextMatrix(130, 150);
			pdfContent.showText(fecha_reserva);
			pdfContent.endText();

			//Dibujo segunda línea
			line.drawLine(pdfWriter.getDirectContent(), 10, 200,  140);
			
			int etiqHoraTamanio = 25;
			int etiqHoraX = 15;
			int etiqHoraY = 85;
			
			int valorHoraTamanio = 40;
			int valorHoraX = 105;
			int valorHoraY = 80;
			
			String serie = sesionMBean.getRecurso().getSerie();
			boolean conSerie = (serie != null) && (serie.length() >= 1);
			
			if (sesionMBean.getRecurso().getMostrarNumeroEnTicket()){
				
				if(! conSerie){
					//Ajusto valor y etiqueta hora
					etiqHoraTamanio = 20;
					etiqHoraX = 15;
					etiqHoraY = 110;
					
					valorHoraTamanio = 30;
					valorHoraX = 120;
					valorHoraY = 107;
					
					//Etiqueta NUMERO
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(15,65);
					pdfContent.showText(getI18N().getText("etiqueta.numero.numero"));
					pdfContent.endText();
		
					//Numero de la reserva
					String nro = sesionMBean.getReservaConfirmada().getNumero().toString();
					int nro_pos = 135;
					
					if (nro.length() == 1){
						nro_pos = 135;
					}else if (nro.length() == 2){
						nro_pos = 125;
					}else{
						nro_pos = 105;
					}
					
					pdfContent.beginText();
					pdfContent.setFontAndSize(symbol, 60);
					pdfContent.setTextMatrix(nro_pos,55);
					pdfContent.showText(nro);
					pdfContent.endText();
				}else{			
					//<<<<<< Agregado >>>>>>
					//Ajusto valor y etiqueta hora
					etiqHoraTamanio = 20;
					etiqHoraX = 15;
					etiqHoraY = 123;
					
					valorHoraTamanio = 20;
					valorHoraX = 120;
					valorHoraY = 122;
					
					//Etiqueta SERIE
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(15,87);
					pdfContent.showText(getI18N().getText("etiqueta.numero.serie"));
					pdfContent.endText();
		
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(120,87);
					pdfContent.showText(serie);
					pdfContent.endText();
					
					//Etiqueta NUMERO
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(15,50);
					pdfContent.showText(getI18N().getText("etiqueta.numero.numero"));
					pdfContent.endText();
					
					//Numero de la reserva
					String nro = sesionMBean.getReservaConfirmada().getNumero().toString();
					int nro_pos = 135;
					
					if (nro.length() == 1){
						nro_pos = 135;
					}else if (nro.length() == 2){
						nro_pos = 125;
					}else{
						nro_pos = 105;
					}
					
					pdfContent.beginText();
					pdfContent.setFontAndSize(symbol, 40);
					pdfContent.setTextMatrix(nro_pos,47);
					pdfContent.showText(nro);
					pdfContent.endText();
				}
			}
			
			//Etiqueta HORA
			pdfContent.beginText();
			pdfContent.setFontAndSize(helveticaBold, etiqHoraTamanio);
			pdfContent.setTextMatrix(etiqHoraX,etiqHoraY);
			pdfContent.showText(getI18N().getText("etiqueta.hora.hora"));
			pdfContent.endText();
			
			//Hora de la reserva
			pdfContent.beginText();
			pdfContent.setFontAndSize(symbol, valorHoraTamanio);
			pdfContent.setTextMatrix(valorHoraX,valorHoraY);
			pdfContent.showText(sdfHr.format(sesionMBean.getDisponibilidad().getHoraInicio()));
			pdfContent.endText();
			
			//Dibujo tercer línea
			line.drawLine(pdfWriter.getDirectContent(), 10, 200,  45);

			String ticketEtiqUno = sesionMBean.getRecurso().getTextoRecurso().getTicketEtiquetaUno();
			String ticketEtiqDos = sesionMBean.getRecurso().getTextoRecurso().getTicketEtiquetaDos();
			int largoEtiqUno = 0;
			int largoEtiqDos = 0;
			int xValores = 0;
			
			if (ticketEtiqUno != null ){
				largoEtiqUno = ticketEtiqUno.length();
			}
			
			if (ticketEtiqDos != null ){
				largoEtiqDos = ticketEtiqDos.length();
			}
			
			if (largoEtiqUno > largoEtiqDos){
				xValores = 8 * (largoEtiqUno + 1); 
			}else{
				xValores = 8 * (largoEtiqDos + 1);
			}
			
			//Etiqueta uno
			if (ticketEtiqUno != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(helveticaBold, 10);
				pdfContent.setTextMatrix(10,30);
				pdfContent.showText(ticketEtiqUno + ":");
				pdfContent.endText();
			}
			
			//Valor etiqueta uno
			String valorEtiqUno = sesionMBean.getRecurso().getTextoRecurso().getValorEtiquetaUno();
			if (valorEtiqUno != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(times, 10);
				pdfContent.setTextMatrix(xValores ,30);
				pdfContent.showText(valorEtiqUno);
				pdfContent.endText();
			}
			
			//Etiqueta dos
			
			if (ticketEtiqDos != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(helveticaBold, 10);
				pdfContent.setTextMatrix(10,15);
				pdfContent.showText(ticketEtiqDos + ":");
				pdfContent.endText();
			}
			
			//Valor etiqueta dos
			String valorEtiqDos = sesionMBean.getRecurso().getTextoRecurso().getValorEtiquetaDos();
			if (valorEtiqDos != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(times, 10);
				pdfContent.setTextMatrix(xValores ,15);
				pdfContent.showText(valorEtiqDos);
				pdfContent.endText();
			}
			
			pdfWriter.addJavaScript("this.print({bUI: true, bSilent: true, bShrinkToFit: true});",false); 
			pdfWriter.addJavaScript("this.closeDoc(true);");   

			document.close();
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response =  (HttpServletResponse)facesContext.getExternalContext().getResponse();
			response.setContentType("application/pdf");  
			
			os.writeTo(response.getOutputStream());
			
			response.getOutputStream().flush();
			response.getOutputStream().close();
			facesContext.responseComplete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String guardarTicket() {
		try {
			
			BaseColor colorBlack = new BaseColor(0,0,0);
			
			BaseFont times = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			BaseFont helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			BaseFont symbol = BaseFont.createFont(BaseFont.SYMBOL, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			
			SimpleDateFormat sdfHr = new SimpleDateFormat ("HH:mm");
			SimpleDateFormat sdfFecha = new SimpleDateFormat ("dd/MM/yyyy");
			
			Rectangle pageSize = new Rectangle(210,210);
			
			Document document = new Document(pageSize);
			document.addTitle(getI18N().getText("etiqueta.reserva.title"));
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			PdfWriter pdfWriter = PdfWriter.getInstance(document, os);
			document.open();
			
			PdfContentByte pdfContent = pdfWriter.getDirectContent();
			
			InputStream is = PasoFinalMBean.class.getResourceAsStream(SAEProfile.getInstance().getProperties().getProperty(SAEProfile.PROFILE_UI_TEMPLATES_IMAGES_LOGO_TICKET_KEY));
			
			byte[] arrImage = new byte [4096];
			is.read(arrImage);
			
			Image img = Image.getInstance(arrImage);
			
			img.scaleAbsolute(100,30);
			img.setAbsolutePosition(55, 170);
			document.add(img);

			//Dibujo primer línea
			LineSeparator line = new LineSeparator();
			line.setAlignment(LineSeparator.ALIGN_CENTER);
			line.setLineColor(colorBlack);
			line.setLineWidth(0.5f);
			
			line.drawLine(pdfContent, 10, 200,  170);

			//Etiqueta RESERVA
			pdfContent.beginText();
			pdfContent.setFontAndSize(helveticaBold, 15);
			pdfContent.setTextMatrix(45, 150);
			pdfContent.showText(getI18N().getText("etiqueta.reserva.showText"));
			pdfContent.endText();
			
			//Fecha de la reserva
			String fecha_reserva = sdfFecha.format(sesionMBean.getDisponibilidad().getHoraInicio());
			
			pdfContent.beginText();
			pdfContent.setFontAndSize(symbol, 16);
			pdfContent.setTextMatrix(130, 150);
			pdfContent.showText(fecha_reserva);
			pdfContent.endText();

			//Dibujo segunda línea
			line.drawLine(pdfWriter.getDirectContent(), 10, 200,  140);
			
			int etiqHoraTamanio = 25;
			int etiqHoraX = 15;
			int etiqHoraY = 85;
			
			int valorHoraTamanio = 40;
			int valorHoraX = 105;
			int valorHoraY = 80;
			
			String serie = sesionMBean.getRecurso().getSerie();
			boolean conSerie = (serie != null) && (serie.length() >= 1);
			
			if (sesionMBean.getRecurso().getMostrarNumeroEnTicket()){
				
				if(! conSerie){
					//Ajusto valor y etiqueta hora
					etiqHoraTamanio = 20;
					etiqHoraX = 15;
					etiqHoraY = 110;
					
					valorHoraTamanio = 30;
					valorHoraX = 120;
					valorHoraY = 107;
					
					//Etiqueta NUMERO
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(15,65);
					pdfContent.showText(getI18N().getText("etiqueta.numero.numero"));
					pdfContent.endText();
		
					//Numero de la reserva
					String nro = sesionMBean.getReservaConfirmada().getNumero().toString();
					int nro_pos = 135;
					
					if (nro.length() == 1){
						nro_pos = 135;
					}else if (nro.length() == 2){
						nro_pos = 125;
					}else{
						nro_pos = 105;
					}
					
					pdfContent.beginText();
					pdfContent.setFontAndSize(symbol, 60);
					pdfContent.setTextMatrix(nro_pos,55);
					pdfContent.showText(nro);
					pdfContent.endText();
				}else{			
					
					//Ajusto valor y etiqueta hora
					etiqHoraTamanio = 20;
					etiqHoraX = 15;
					etiqHoraY = 123;
					
					valorHoraTamanio = 20;
					valorHoraX = 120;
					valorHoraY = 122;
					
					//Etiqueta SERIE
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(15,87);
					pdfContent.showText(getI18N().getText("etiqueta.numero.serie"));
					pdfContent.endText();
		
					pdfContent.beginText();
					pdfContent.setFontAndSize(symbol, 20);
					pdfContent.setTextMatrix(120,87);
					pdfContent.showText(serie);
					pdfContent.endText();
					
					//Etiqueta NUMERO
					pdfContent.beginText();
					pdfContent.setFontAndSize(helveticaBold, 20);
					pdfContent.setTextMatrix(15,50);
					pdfContent.showText(getI18N().getText("etiqueta.numero.numero"));
					pdfContent.endText();
					
					//Numero de la reserva
					String nro = sesionMBean.getReservaConfirmada().getNumero().toString();
					int nro_pos = 135;
					
					if (nro.length() == 1){
						nro_pos = 135;
					}else if (nro.length() == 2){
						nro_pos = 125;
					}else{
						nro_pos = 105;
					}
					
					pdfContent.beginText();
					pdfContent.setFontAndSize(symbol, 40);
					pdfContent.setTextMatrix(nro_pos,47);
					pdfContent.showText(nro);
					pdfContent.endText();
				}
			}
			
			//Etiqueta HORA
			pdfContent.beginText();
			pdfContent.setFontAndSize(helveticaBold, etiqHoraTamanio);
			pdfContent.setTextMatrix(etiqHoraX,etiqHoraY);
			pdfContent.showText(getI18N().getText("etiqueta.hora.hora"));
			pdfContent.endText();
			
			//Hora de la reserva
			pdfContent.beginText();
			pdfContent.setFontAndSize(symbol, valorHoraTamanio);
			pdfContent.setTextMatrix(valorHoraX,valorHoraY);
			pdfContent.showText(sdfHr.format(sesionMBean.getDisponibilidad().getHoraInicio()));
			pdfContent.endText();
			
			//Dibujo tercer línea
			line.drawLine(pdfWriter.getDirectContent(), 10, 200,  45);
			
			String ticketEtiqUno = sesionMBean.getRecurso().getTextoRecurso().getTicketEtiquetaUno();
			String ticketEtiqDos = sesionMBean.getRecurso().getTextoRecurso().getTicketEtiquetaDos();
			int largoEtiqUno = 0;
			int largoEtiqDos = 0;
			int xValores = 0;
			
			if (ticketEtiqUno != null ){
				largoEtiqUno = ticketEtiqUno.length();
			}
			
			if (ticketEtiqDos != null ){
				largoEtiqDos = ticketEtiqDos.length();
			}
			
			if (largoEtiqUno > largoEtiqDos){
				xValores = 8 * (largoEtiqUno + 1); 
			}else{
				xValores = 8 * (largoEtiqDos + 1);
			}
			
			//Etiqueta uno
			if (ticketEtiqUno != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(helveticaBold, 10);
				pdfContent.setTextMatrix(10,30);
				pdfContent.showText(ticketEtiqUno + ":");
				pdfContent.endText();
			}
			
			//Valor etiqueta uno
			String valorEtiqUno = sesionMBean.getRecurso().getTextoRecurso().getValorEtiquetaUno();
			if (valorEtiqUno != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(times, 10);
				pdfContent.setTextMatrix(xValores ,30);
				pdfContent.showText(valorEtiqUno);
				pdfContent.endText();
			}
			
			//Etiqueta dos
			if (ticketEtiqDos != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(helveticaBold, 10);
				pdfContent.setTextMatrix(10,15);
				pdfContent.showText(ticketEtiqDos + ":");
				pdfContent.endText();
			}
			
			//Valor etiqueta dos
			String valorEtiqDos = sesionMBean.getRecurso().getTextoRecurso().getValorEtiquetaDos();
			if (valorEtiqDos != null){
				pdfContent.beginText();
				pdfContent.setFontAndSize(times, 10);
				pdfContent.setTextMatrix(xValores ,15);
				pdfContent.showText(valorEtiqDos);
				pdfContent.endText();
			}
			
			document.close();
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response =  (HttpServletResponse)facesContext.getExternalContext().getResponse();
			response.setContentType("application/pdf");  
			response.setHeader("Content-disposition", "attachment; filename=" + "Ticket de confirmación.pdf");
			
			os.writeTo(response.getOutputStream());
			
			response.getOutputStream().flush();
			response.getOutputStream().close();
			facesContext.responseComplete();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}

	
	
