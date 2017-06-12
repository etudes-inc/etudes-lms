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

package uk.ac.lancs.e_science.sakai.tools.blogger;


import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;


public class AddCommentController extends BloggerController{
    private Post post;
    private String commentText;
    private Blogger blogger;

    public AddCommentController(){
    	super();
    	blogger = BloggerManager.getBlogger();
    }
    
    public void setPost(Post post){
        this.post = post;
    }

    public Post getPost(){
        return post;
    }
    public void setCommentText(String text){
        commentText = text;
    }
    public String getCommentText(){
        return ""; //initially, the comment text is empty.
    }
    public String doSaveComment(){
        Comment comment = new Comment(commentText);
        blogger.addCommentToPost(comment, post.getOID(),getCurretUserId(), getCurrentSiteId());
        return "viewPost";

    }
}
