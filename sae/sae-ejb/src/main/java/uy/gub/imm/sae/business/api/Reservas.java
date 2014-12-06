package uy.gub.imm.sae.business.api;

import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;

public interface Reservas {
	public void modificarEstadoReserva(ReservaDTO reserva) throws AccesoMultipleException;
	
}
