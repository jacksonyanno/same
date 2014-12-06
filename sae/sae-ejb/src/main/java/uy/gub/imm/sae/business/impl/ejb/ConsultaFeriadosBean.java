package uy.gub.imm.sae.business.impl.ejb;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import uy.gub.imm.sae.business.api.ConsultaFeriadosLocal;
import uy.gub.imm.sae.business.api.ConsultaFeriadosRemote;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_PLANIFICADOR","RA_AE_FCALL_CENTER","RA_AE_FATENCION", "RA_AE_ANONIMO"})
public class ConsultaFeriadosBean implements ConsultaFeriadosRemote, ConsultaFeriadosLocal {

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;
	
    public ConsultaFeriadosBean() {

    }

    @SuppressWarnings (value = "unchecked")
    public Collection<Date> obtenerDiasFeriados (){
    	
    	return (List<Date>) entityManager.createQuery("SELECT distinct f.dia FROM Feriados f" ).getResultList();
    }
}
