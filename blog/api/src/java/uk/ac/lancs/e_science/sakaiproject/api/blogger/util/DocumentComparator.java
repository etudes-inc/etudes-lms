/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/

package uk.ac.lancs.e_science.sakaiproject.api.blogger.util;


import java.util.Comparator;

public class DocumentComparator implements Comparator {
	private String _campoOrdenacion;
	private boolean _caseSensitive;
	private boolean _ordenDescendente;
	public DocumentComparator(String campoOrdenacion,boolean caseSensitive,boolean ordenDescendente){
		_campoOrdenacion = campoOrdenacion;
		_caseSensitive = caseSensitive;
		_ordenDescendente = ordenDescendente;
	}
	public int compare(Object obj1, Object obj2){
		if (_ordenDescendente)
			return (-1)*comparacion(obj1,obj2);
		return comparacion(obj1,obj2);

	}
	public boolean equal(Object o){
		return this.equal(o);
	}
	private int comparacion(Object obj1, Object obj2){
		String doc1=(String)obj1;
		String doc2=(String)obj2;
		String campoEnDoc1 = XMLHelper.dameTextoDeLaEtiqueta(_campoOrdenacion,doc1);
		String campoEnDoc2 = XMLHelper.dameTextoDeLaEtiqueta(_campoOrdenacion,doc2);
		if (campoEnDoc1==null && campoEnDoc2!=null)
			return 1;
		if (campoEnDoc2==null && campoEnDoc1!=null)
			return -1;
		if (campoEnDoc1==null && campoEnDoc2==null)
			return 0;
		try{
			double double1 = new Double(campoEnDoc1).doubleValue();
			double double2 = new Double(campoEnDoc2).doubleValue();
			if (double1>double2)
				return 1;
			if (double1==double2)
				return 0;
			if (double1<double2)
				return -1;
		} catch (NumberFormatException e){
			//si se da esta excepcion es que no se pueden comparar como numeros
		}
		if (_caseSensitive)
			return campoEnDoc1.compareTo(campoEnDoc2);
		return campoEnDoc1.compareToIgnoreCase(campoEnDoc2);
	}
}
