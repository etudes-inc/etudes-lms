/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-impl/impl/src/java/uk/ac/cam/caret/sakai/rwiki/component/dao/impl/IteratorProxy.java $
 * $Id: IteratorProxy.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.Iterator;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;

/**
 * Provides a proxy implementation of an iterator, proxying objects with a
 * ObjectProxy
 * 
 * @author ieb
 */
// FIXME: Component
public class IteratorProxy implements Iterator
{

	private ObjectProxy lop;

	private Iterator i;

	public IteratorProxy(Iterator i, ObjectProxy lop)
	{
		this.i = i;
		this.lop = lop;
	}

	public boolean hasNext()
	{
		return i.hasNext();
	}

	public Object next()
	{
		return lop.proxyObject(i.next());
	}

	public void remove()
	{
		i.remove();
	}

}
