import subprocess,json,unicodedata,re
source='galenos_rest_test_gt02_presentacion_20261113'
target='clinica'
tables=['clinica','rol','tipo_documento','tipo_medio_contacto','tipo_examen','examen','persona','procedimiento','persona_rol','documento','medio_contacto','examen_tipo_examen','procedimiento_paso','procedimiento_paso_secuencia','procedimiento_paso_examen','consulta','consulta_procedimiento','consulta_procedimiento_paso','orden_examen','examen_resultado']
def query(db,sql):
 return subprocess.check_output(['docker','exec','postgres-db','psql','-X','-U','postgres','-d',db,'-At','-v','ON_ERROR_STOP=1','-c',sql],text=True)
def rows(db,table): return json.loads(query(db,'SELECT coalesce(json_agg(t),\'[]\'::json) FROM public.'+table+' t'))
def normal(s):
 s=''.join(c for c in unicodedata.normalize('NFD',str(s or '')) if not unicodedata.combining(c)).lower()
 return ' '.join(s.replace('galenossv','').split())
def key(table,row):
 if table=='persona':return normal(row['nombres'])+' '+normal(row['apellidos'])
 if table in ['clinica','rol','tipo_documento','tipo_medio_contacto','tipo_examen','examen','procedimiento']:return normal(row['nombre'])
 if table=='procedimiento_paso':return (row['id_procedimiento'],normal(row['nombre']))
 if table=='persona_rol':return (row['id_persona'],row['id_rol'],row['id_clinica'])
 return None
def literal(v):
 if v is None:return 'NULL'
 if isinstance(v,bool):return 'true' if v else 'false'
 if isinstance(v,(float,int)):return str(v)
 return "'"+str(v).replace("'","''")+"'"
mapping={};statements=['BEGIN;'];counts=[]
def columns(db):
 return json.loads(query(db,"SELECT json_agg(t) FROM (SELECT table_name,column_name,data_type,character_maximum_length FROM information_schema.columns WHERE table_schema='public') t"))
target_cols={(c['table_name'],c['column_name']) for c in columns(target)}
for c in columns(source):
 if c['table_name'] not in tables or (c['table_name'],c['column_name']) in target_cols: continue
 datatype=c['data_type']
 if datatype=='character varying' and c['character_maximum_length']: datatype+='('+str(c['character_maximum_length'])+')'
 statements.append('ALTER TABLE public.'+c['table_name']+' ADD COLUMN IF NOT EXISTS '+c['column_name']+' '+datatype+';')
 print('Campo requerido:',c['table_name'],c['column_name'])

for table in tables:
 pk='id_'+table
 existing=rows(target,table)
 index={key(table,r):r[pk] for r in existing if key(table,r) is not None}
 existing_ids={r[pk] for r in existing}
 count=0
 for original in rows(source,table):
  row={k:mapping.get(v,v) if isinstance(v,str) else v for k,v in original.items()}
  natural=key(table,row)
  found=index.get(natural) if natural is not None else None
  if found:
   mapping[original[pk]]=found
   continue
  mapping[original[pk]]=row[pk]
  if row[pk] in existing_ids:continue
  cols=list(row)
  statements.append('INSERT INTO public.'+table+' ('+','.join(cols)+') VALUES ('+','.join(literal(row[k]) for k in cols)+') ON CONFLICT ('+pk+') DO NOTHING;')
  if natural is not None:index[natural]=row[pk]
  count+=1
 counts.append((table,count))
statements.append('COMMIT;')
subprocess.run(['docker','exec','-i','postgres-db','psql','-X','-U','postgres','-d',target,'-v','ON_ERROR_STOP=1','-q'],input='\n'.join(statements),text=True,check=True)
print('Importación en clinica completada sin eliminar registros:')
for table,count in counts:print(table,count)
