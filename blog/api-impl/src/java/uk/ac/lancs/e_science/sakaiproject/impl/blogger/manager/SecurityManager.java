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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger.manager;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.PersistenceException;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.SakaiPersistenceManager;


public class SecurityManager {
	private SakaiPersistenceManager persistenceManager;
	
	public SecurityManager() throws PersistenceException{
		persistenceManager = new SakaiPersistenceManager();
	}
	
    public boolean isAllowedToStorePost(String userId, Post post){
        if (post.getCreator()==null)
            return true;
        if (post.getState().getVisibility()==State.PRIVATE && !post.getCreator().getId().equals(userId))
            return false;
        return true;
    }
    public boolean isAllowedToDeletePost(String userId, String postId) throws PersistenceException{
    	Post post = persistenceManager.getPost(postId);
        if (post.getState().getVisibility()==State.PRIVATE && !post.getCreator().getId().equals(userId))
            return false;
        return true;
    }
    public boolean isAllowedToComment(String userId, String postId) throws PersistenceException{
    	Post post = persistenceManager.getPost(postId);
        if (post.getState().getVisibility()==State.SITE)
            return true;
        if (post.getState().getVisibility()==State.PUBLIC)
            return true;
        if (post.getState().getVisibility()==State.PRIVATE && post.getCreator().getId().equals(userId))
            return true;
        return false;
    }

    public Post filterSearch(String userId, Post post){

        //String idCreator = post.getCreator().getId();
        int visibility = post.getState().getVisibility();
        if (visibility == State.PUBLIC)
            return post;
        else if (visibility == State.SITE)
            return post;
        else if (visibility == State.PRIVATE && userId.equals(post.getCreator().getId()))
            return post;
        //in other combinations, we dont allow to see the post.
        return null;

    }

    public List filterSearch(String userId, List posts){
        ArrayList result = new ArrayList();
        Iterator it = posts.iterator();
        while (it.hasNext()){
            Post post = (Post)it.next();

            int visibility = post.getState().getVisibility();
            if (visibility == State.PUBLIC)
                result.add(post);
            else if (visibility == State.SITE)
                result.add(post);
            else if (visibility == State.PRIVATE && userId.equals(post.getCreator().getId()))
                result.add(post);
            //in other combinations, we dont allow to see the post.
        }
        return result;
    }
}
