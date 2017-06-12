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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface XMLPostContentHandleState {
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException;
    public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException;
    public XMLPostContentHandleState characters(char ch[], int start, int length) throws SAXException;

}
