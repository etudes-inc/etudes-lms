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

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class FileProcessingState implements XMLPostContentHandleState {
    private Post _post;
    private String _currentText;
    private File _fileUnderConstruction;
    private XMLPostContentHandleState _previousState;

    public FileProcessingState(Post post, XMLPostContentHandleState previousState){
        _currentText = new String();
        _previousState = previousState;
        _post = post;
        _fileUnderConstruction= new File();
    }
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        return this;
    }
    public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException{
       if (localName.equals("file")){
            _post.addElement(_fileUnderConstruction);
            return _previousState;
        }
        if (localName.equals("fileId")){
            _fileUnderConstruction.setIdFile(_currentText);
            _currentText = new String();
            return this;
        }
        if (localName.equals("fileDescription")){
        	_fileUnderConstruction.setDescription(_currentText);
            _currentText = new String();
            return this;

        }
        return this;
    }
    public XMLPostContentHandleState characters(char ch[], int start, int length) throws SAXException{
        if (_currentText == null) {
            _currentText = new String(ch, start, length);
        } else {
            _currentText+=new String(ch,start, length);
        }
        return this;
    }
}
