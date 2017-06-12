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

import java.util.Collections;
import java.util.List;


public class DocumentSorter {
	/**
	 * Ordena una lista de documentos en orden ascendente seg&uacute;n los valores del campo de ordenaci&oacute;n
	 * La lista especificable debe de ser modificable. Este algoritmo tiene una complejidad n(log n).
	 * Internamente utiliza Collections.sort
	 * La ordenacion se hace ascendentemente, es decir,de menor a mayor
	 * 
	 * 
	 * @param listaDocumentos  Este lista queda ordenada despues de ejecutar este metodo
	 * @param campo
	 * @return la lista ordenada. Es exactamente le parametro listaDocumentos.
	 */
	public static List clasificaLista(List listaDocumentos, String campo){
		boolean sensibleAMayusculas = false;
		DocumentComparator comparador = new DocumentComparator(campo,sensibleAMayusculas,false);
		Collections.sort(listaDocumentos,comparador);
		return listaDocumentos;
	}
	/**
	 * Igual que el metodo clasificaLista(List, String), solo que ordena de manera descendente, es decir, de mayor a menor
	 * @param listaDocumentos
	 * @param campo
	 * @return
	 */
	public static List clasificaListaEnOrdenDescendente(List listaDocumentos, String campo){
		boolean sensibleAMayusculas = false;
		DocumentComparator comparador = new DocumentComparator(campo,sensibleAMayusculas,true);
		Collections.sort(listaDocumentos,comparador);
		return listaDocumentos;
	}
	

}
