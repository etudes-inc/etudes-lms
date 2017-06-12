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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.*;

public class PostReader {

    private PostConverter _converter;
    public PostReader(PostConverter converter){
        _converter = converter;
    }
    public void parsePost(Post post){
        _converter.reset();
        _converter.convertOID(post.getOID());
        _converter.convertTitle(post.getTitle());
        _converter.convertShortText(post.getShortText());
        _converter.convertDatePost(post.getDate());
        _converter.convertKeywords(post.getKeywords());
        if (post.getCreator()!=null)
            _converter.convertCreator(post.getCreator());
        _converter.convertState(post.getState());
        if (post.getElements()!=null){
        	for (int i=0;i<post.getElements().length;i++){
	            PostElement element = post.getElements()[i];
	            if (element instanceof Paragraph)
	                _converter.convertParagraph((Paragraph)element);
	            if (element instanceof Image)
	                _converter.convertImage((Image)element);
	            if (element instanceof LinkRule)
	                _converter.convertLinkRules((LinkRule)element);
	            if (element instanceof File)
	                _converter.convertFile((File)element);
	        }
        }
        _converter.convertComments(post.getComments());
    }
}
