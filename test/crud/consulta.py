"""ConsultaFlujoService mediante Liberty y PostgreSQL sintéticos, sin mocks."""
import unittest
from catalogos import http
C='00000000-0000-4000-8000-000000000011'
P='00000000-0000-4000-8000-000000000101'
T='00000000-0000-4000-8000-000000005001'
Q='?idClinica='+C
class ConsultaHTTP(unittest.TestCase):
    def test_clinic_scope_and_patient_search(self):
        self.assertEqual(http('clinica_disponible')[1]['Total-registros'],'1')
        self.assertEqual(http('consulta'+Q,role='otro')[0],403)
        self.assertEqual(http('consulta?idClinica=00000000-0000-4000-8000-000000000012')[0],403)
        rows=http('persona_rol/pacientes'+Q+'&buscar=Persona')[2]
        self.assertEqual([p['idPersonaRol'] for p in rows],[P])
        self.assertEqual(http('persona_rol/pacientes'+Q+'&buscar=NoExiste')[2],[])
    def test_persist_edit_associate_automatic_step_and_invalid_state(self):
        data={'idPersonaRol':P,'fechaInicio':'2026-10-30T10:00:00Z','fechaFin':None,'referenciaExterna':' Prueba sintética ','observaciones':'HTTP real aislado'}
        status,headers,created=http('consulta'+Q,'POST',data);self.assertEqual(status,201);ident=created['idConsulta']
        self.assertTrue(headers['Location'].endswith(ident+Q));self.assertEqual(created['referenciaExterna'],'Prueba sintética')
        self.assertEqual(http('consulta/'+ident+Q)[2],created)
        changed=dict(data,observaciones='Edición persistida')
        self.assertEqual(http('consulta/'+ident+Q,'PUT',changed)[0],200)
        self.assertEqual(http('consulta/'+ident+Q)[2]['observaciones'],'Edición persistida')
        invalid=dict(changed,idPersonaRol='00000000-0000-4000-8000-000000000104')
        self.assertEqual(http('consulta/'+ident+Q,'PUT',invalid)[0],409)
        proc={'idConsulta':ident,'idProcedimiento':T,'fechaInicio':data['fechaInicio'],'fechaFin':None,'observaciones':'Asociación sintética'}
        status,_,association=http('consulta_procedimiento'+Q,'POST',proc);self.assertEqual(status,201)
        self.assertEqual(http('consulta_procedimiento'+Q+'&idConsulta='+ident)[1]['Total-registros'],'1')
        steps=http('consulta_procedimiento_paso'+Q+'&idConsulta='+ident)[2];self.assertEqual(len(steps),1);step=steps[0]
        self.assertEqual(step['estado'],'PENDIENTE');step_id=step.pop('idConsultaProcedimientoPaso')
        self.assertEqual(http('consulta_procedimiento_paso/'+step_id+Q,'PUT',dict(step,estado='INVENTADO'))[0],422)
        self.assertEqual(http('consulta_procedimiento_paso/'+step_id+Q)[2]['estado'],'PENDIENTE')
        self.assertEqual(http('consulta_procedimiento_paso/'+step_id+Q,'PUT',dict(step,estado='EN_CURSO'))[0],200)
        self.assertEqual(http('consulta_procedimiento_paso/'+step_id+Q)[2]['estado'],'EN_CURSO')
        self.assertEqual(http('consulta_procedimiento_paso'+Q,'POST',step)[0],405)
        self.assertEqual(http('consulta/'+ident+Q,'DELETE')[0],405)
    def test_invalid_foreign_patient_dates_and_missing_are_distinct(self):
        data={'idPersonaRol':'00000000-0000-4000-8000-000000000102','fechaInicio':'2026-10-30T10:00:00Z'}
        self.assertEqual(http('consulta'+Q,'POST',data)[0],422)
        data['idPersonaRol']=P;data['fechaFin']='2026-10-29T10:00:00Z'
        self.assertEqual(http('consulta'+Q,'POST',data)[0],422)
        self.assertEqual(http('consulta/00000000-0000-4000-8000-000000999999'+Q)[0],404)
if __name__=='__main__':unittest.main(verbosity=2)
