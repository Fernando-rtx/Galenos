"""Personas y relaciones persistidas en galenos_demo_crud sintética, HTTP real."""
import unittest,uuid,subprocess
from catalogos import http,CONTAINER
class PersonaHTTP(unittest.TestCase):
 def test_persona_documents_contacts_and_roles(self):
  suffix=uuid.uuid4().hex[:12];name='Persona '+suffix.translate(str.maketrans('0123456789','abcdefghij'))
  data={'nombres':name,'apellidos':'Prueba Sintética','fechaNacimiento':'2000-01-01T06:00:00Z'}
  status,headers,p=http('persona','POST',data);self.assertEqual(status,201);person=p['idPersona'];self.assertTrue(headers['Location'].endswith(person))
  self.assertEqual(http('persona/'+person)[2],p)
  self.assertEqual(http('persona/'+person,'PUT',dict(data,nombres=name+' Editada'))[0],200)
  self.assertEqual(http('persona/'+person)[2]['nombres'],name+' Editada')
  self.assertEqual(http('persona/'+person,'DELETE')[0],405)
  status,_,tipo=http('tipo_documento','POST',{'nombre':'Tipo sintético '+suffix,'activo':True});self.assertEqual(status,201)
  doc={'idPersona':person,'idTipoDocumento':tipo['idTipoDocumento'],'valor':'DOC-'+suffix,'rutaFisica':None}
  status,_,d=http('documento','POST',doc);self.assertEqual(status,201);docid=d['idDocumento']
  self.assertEqual(http('documento','POST',doc)[0],409)
  self.assertEqual(http('documento/'+docid,'PUT',dict(doc,valor='EDIT-'+suffix))[0],200)
  self.assertEqual(http('documento/'+docid)[2]['valor'],'EDIT-'+suffix)
  bad=dict(doc,idTipoDocumento='00000000-0000-4000-8000-000000000001')
  self.assertEqual(http('documento/'+docid,'PUT',bad)[0],422)
  contact={'idPersona':person,'idTipoMedioContacto':'00000000-0000-4000-8000-000000011001','valor':suffix+'@example.invalid'}
  status,_,c=http('medio_contacto','POST',contact);self.assertEqual(status,201);cid=c['idMedioContacto']
  self.assertEqual(http('medio_contacto','POST',contact)[0],409)
  self.assertEqual(http('medio_contacto/'+cid,'PUT',contact)[0],200)
  self.assertEqual(http('medio_contacto/'+cid,'PUT',dict(contact,valor='sin-arroba'))[0],422)
  assignment={'idPersona':person,'idClinica':'00000000-0000-4000-8000-000000000011','idRol':'00000000-0000-4000-8000-000000000031'}
  status,_,a=http('persona_rol','POST',assignment);self.assertEqual(status,201);aid=a['idPersonaRol']
  self.assertEqual(http('persona_rol','POST',assignment)[0],409)
  self.assertEqual(http('persona_rol/'+aid,'PUT',assignment)[0],200)
  self.assertEqual(http('persona_rol','POST',dict(assignment,idClinica='00000000-0000-4000-8000-000000000012'))[0],403)
  for route,key,ident in [('documento','idDocumento',docid),('medio_contacto','idMedioContacto',cid),('persona_rol','idPersonaRol',aid)]:
   rows=http(route+'?idPersona='+person+'&first=0&max=1');self.assertEqual(rows[0],200);self.assertEqual(rows[1]['Total-registros'],'1');self.assertEqual(rows[2][0][key],ident)
  uuid.UUID(person)
  count=subprocess.check_output(['docker','exec',CONTAINER,'psql','-U','galenos_demo','-d','galenos_demo_crud','-At','-v','ON_ERROR_STOP=1','-c',"SELECT count(*) FROM persona WHERE id_persona='"+person+"' AND nombres LIKE '% Editada'"],text=True).strip();self.assertEqual(count,'1')
  for route,ident in [('documento',docid),('medio_contacto',cid),('persona_rol',aid)]:
   self.assertEqual(http(route+'/'+ident,'DELETE')[0],204);self.assertEqual(http(route+'/'+ident)[0],404)
 def test_dui_age_guard_uses_persisted_case_insensitive_type(self):
  data={'nombres':'Persona Adulta','apellidos':'Prueba Sintética','fechaNacimiento':'2000-01-01T06:00:00Z'}
  status,_,p=http('persona','POST',data);self.assertEqual(status,201);person=p['idPersona']
  status,_,t=http('tipo_documento','POST',{'nombre':'DUI','activo':True})
  if status==409:
   rows=http('tipo_documento?nombre=DUI&max=100')[2];t=next(x for x in rows if x['nombre'].strip().lower()=='dui')
  else:self.assertEqual(status,201)
  status,_,d=http('documento','POST',{'idPersona':person,'idTipoDocumento':t['idTipoDocumento'],'valor':uuid.uuid4().hex})
  self.assertEqual(status,201)
  self.assertEqual(http('persona/'+person,'PUT',dict(data,fechaNacimiento='2020-01-01T06:00:00Z'))[0],422)
  self.assertEqual(http('persona/'+person)[2]['fechaNacimiento'],data['fechaNacimiento'])
  self.assertEqual(http('documento/'+d['idDocumento'],'DELETE')[0],204)
 def test_invalid_person_input_and_in_use_role(self):
  for data in [{'nombres':'X','apellidos':'Prueba','fechaNacimiento':'2000-01-01T06:00:00Z'}, {'nombres':'Prueba','apellidos':'Sintética','fechaNacimiento':'2999-01-01T06:00:00Z'}]:
   self.assertEqual(http('persona','POST',data)[0],422)
  ident='00000000-0000-4000-8000-000000000101'
  self.assertEqual(http('persona_rol/'+ident,'DELETE')[0],409);self.assertEqual(http('persona_rol/'+ident)[0],200)
if __name__=='__main__':unittest.main(verbosity=2)
