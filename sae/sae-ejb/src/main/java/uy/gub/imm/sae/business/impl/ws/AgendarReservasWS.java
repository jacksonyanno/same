package uy.gub.imm.sae.business.impl.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import org.jboss.wsf.spi.annotation.WebContext;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.IAgendarReservasWS;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorValidacionCommitException;
import uy.gub.imm.sae.common.exception.ErrorValidacionException;
import uy.gub.imm.sae.common.exception.UserCommitException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.exception.ValidacionClaveUnicaException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionPorCampoException;
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.common.factories.BusinessLocatorFactory;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

@Stateless
@WebService(serviceName = "AgendarReservasService",  targetNamespace = "http://montevideo.gub.uy/schema/sae/1.0/", portName = "AgendarReservasPort")
@WebContext(urlPattern = "/AgendarReservas")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
public class AgendarReservasWS implements IAgendarReservasWS {
	
	
	@WebMethod
	public @WebResult(name = "confirmarReservaResult") Reserva confirmarReserva
		(
			@WebParam(name = "reserva") Reserva r,
			@WebParam(name = "cancelarReservasPrevias") Boolean cancelarReservasPrevias,
			@WebParam(name = "confirmarConWarning") Boolean confirmarConWarning
		) throws ApplicationException, BusinessException, AccesoMultipleException, 
			ValidacionException, UserException, WarningValidacionException, ErrorValidacionException, WarningValidacionCommitException, 
			ErrorValidacionCommitException, ValidacionClaveUnicaException, ValidacionPorCampoException{
		
		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}
	
		return agendarReservasSession.confirmarReserva(r, cancelarReservasPrevias, confirmarConWarning);
	}

	@WebMethod
	public @WebResult(name = "consultarAgendaPorNombreResult") Agenda consultarAgendaPorNombre
		(
			@WebParam(name = "nombre") String nombre
		)
			throws ApplicationException, BusinessException {

		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}
		
		return agendarReservasSession.consultarAgendaPorNombre(nombre);
	}

	@WebMethod
	public @WebResult(name = "consultarRecursosResult") ArrayList<Recurso> consultarRecursos
		(
			@WebParam(name = "agenda") Agenda a
		) 
			throws ApplicationException, BusinessException {
		
		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}
		
		List<Recurso> lst = agendarReservasSession.consultarRecursos(a);
		
		for (int i=0; i<lst.size();i++){
			lst.get(i).getDatoDelRecurso().size();
			List<AgrupacionDato> agDatos = lst.get(i).getAgrupacionDatos();
			
			for (int j=0; j<agDatos.size();j++){
				List<DatoASolicitar> datos = agDatos.get(j).getDatosASolicitar();
				
				for (int k=0; k<datos.size();k++){
					datos.get(k).getValoresPosibles().size();
				}
			}
		}

		if (lst instanceof ArrayList){
			return (ArrayList<Recurso>)lst;
		}else{
			ArrayList<Recurso> arrLst = new ArrayList<Recurso>(lst);			
			return arrLst;
		}
	}

	@WebMethod
	public @WebResult(name = "consultarReservaPorDatosClaveResult") Reserva consultarReservaPorDatosClave
		(
			@WebParam(name = "recurso") Recurso r, 
			@WebParam(name = "datos") HashMap<DatoASolicitar, DatoReserva> datos
		) 
			throws ApplicationException{

		AgendarReservas agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		
		return agendarReservasSession.consultarReservaPorDatosClave(r, datos);
	}

	@WebMethod
	public void desmarcarReserva(@WebParam(name = "reserva") Reserva r) throws BusinessException {
		
		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}

		agendarReservasSession.desmarcarReserva(r);
	}

	@WebMethod(operationName="marcarReservaDisponible")
	public @WebResult(name = "marcarReservaDisponibleResult") Reserva marcarReserva
		(
			@WebParam(name = "disponibilidad") Disponibilidad d
		) 
			throws BusinessException, UserException, UserCommitException {
		
		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}

		return agendarReservasSession.marcarReserva(d);
	}

	@WebMethod
	public @WebResult(name = "obtenerCuposPorDiaResult") ArrayList<Integer> obtenerCuposPorDia
		(
			@WebParam(name = "recurso") Recurso r, 
			@WebParam(name = "ventanaDeTiempo") VentanaDeTiempo v
		)
			throws BusinessException {
		
		AgendarReservas agendarReservasSession = null;
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}
		
		List<Integer> lst = agendarReservasSession.obtenerCuposPorDia(r, v);
		
		if (lst instanceof ArrayList){
			return (ArrayList<Integer>)lst;
		}else{
			ArrayList<Integer> arrLst = new ArrayList<Integer>(lst);			
			return arrLst;
		}
	}

	@WebMethod
	public @WebResult(name = "obtenerDisponibilidadesResult") ArrayList<Disponibilidad> obtenerDisponibilidades
		(
			@WebParam(name = "recurso") Recurso r, 
			@WebParam(name = "ventanaDeTiempo") VentanaDeTiempo v
		) 
			throws BusinessException {
		
		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}
		
		List<Disponibilidad> lst = agendarReservasSession.obtenerDisponibilidades(r, v);
		
		if (lst instanceof ArrayList){
			return (ArrayList<Disponibilidad>)lst;
		}else{
			ArrayList<Disponibilidad> arrLst = new ArrayList<Disponibilidad>(lst);
			return arrLst;
		}
	}

	@WebMethod
	public @WebResult(name = "obtenerVentanaCalendarioInternetResult") VentanaDeTiempo obtenerVentanaCalendarioInternet
		(
			@WebParam(name = "recurso") Recurso r
		) 
			throws BusinessException {

		AgendarReservas agendarReservasSession = null;
		
		try{
			agendarReservasSession = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
		}catch (ApplicationException e){
			throw new BusinessException(e.getCodigoError(),e.getMessage(),e.getCause());
		}
		
		return agendarReservasSession.obtenerVentanaCalendarioInternet(r);
	}

	@WebMethod
	public @WebResult(name = "pingResult") String ping() {
		return "pong";
	}
}
