package uy.gub.imm.sae.business.api;


import java.util.ArrayList;
import java.util.HashMap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

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
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

@WebService
public interface IAgendarReservasWS {

	@WebMethod
	public @WebResult(name = "confirmarReservaResult") Reserva confirmarReserva
		(
			@WebParam(name = "reserva") Reserva r,
			@WebParam(name = "cancelarReservasPrevias") Boolean cancelarReservasPrevias,
			@WebParam(name = "confirmarConWarning") Boolean confirmarConWarning
		)
		throws 
			ApplicationException, BusinessException,
			AccesoMultipleException, ValidacionException, UserException,
			WarningValidacionException, ErrorValidacionException,
			WarningValidacionCommitException, ErrorValidacionCommitException,
			ValidacionClaveUnicaException;

	@WebMethod
	public @WebResult(name = "consultarAgendaPorNombreResult") Agenda consultarAgendaPorNombre
		(
			@WebParam(name = "nombre") String nombre
		)
		throws 
			ApplicationException, BusinessException;

	@WebMethod
	public @WebResult(name = "consultarRecursosResult") ArrayList<Recurso> consultarRecursos
		(
			@WebParam(name = "agenda") Agenda a
		) 
		throws 
			ApplicationException, BusinessException;

	@WebMethod
	public @WebResult(name = "consultarReservaPorDatosClaveResult") Reserva consultarReservaPorDatosClave
		(
			@WebParam(name = "recurso") Recurso r, 
			@WebParam(name = "datos") HashMap<DatoASolicitar, DatoReserva> datos
		) 
		throws 
			ApplicationException;

	@WebMethod
	public void desmarcarReserva
		(
			@WebParam(name = "reserva") Reserva r
		) 
		throws 
			BusinessException;

	@WebMethod(operationName = "marcarReservaDisponible")
	public @WebResult(name = "marcarReservaDisponibleResult") Reserva marcarReserva
		(
			@WebParam(name = "disponibilidad") Disponibilidad d
		) 
		throws 
			BusinessException, UserException, UserCommitException;

	@WebMethod
	public @WebResult(name = "obtenerCuposPorDiaResult")  ArrayList<Integer> obtenerCuposPorDia
		(
			@WebParam(name = "recurso") Recurso r, 
			@WebParam(name = "ventanaDeTiempo") VentanaDeTiempo v
		)
		throws 
			BusinessException;

	@WebMethod
	public @WebResult(name = "obtenerDisponibilidadesResult") ArrayList<Disponibilidad> obtenerDisponibilidades
		(
			@WebParam(name = "recurso") Recurso r, 
			@WebParam(name = "ventanaDeTiempo") VentanaDeTiempo v
		)
		throws 
			BusinessException;

	@WebMethod
	public @WebResult(name = "obtenerVentanaCalendarioInternetResult") VentanaDeTiempo obtenerVentanaCalendarioInternet
		(
			@WebParam(name = "recurso") Recurso r
		)
		throws 
			BusinessException;
	
	@WebMethod
	public  @WebResult(name = "pingResult") String ping();
}
