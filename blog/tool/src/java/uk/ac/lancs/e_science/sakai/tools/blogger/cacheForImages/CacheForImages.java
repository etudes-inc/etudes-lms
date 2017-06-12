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
package uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages;


import java.util.Hashtable;
import java.util.Timer;


import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;

public class CacheForImages {
	private static CacheForImages imageCache = null;
	private Timer timer;
	private Hashtable tableImages;
	private CacheForImages(){
		tableImages = new Hashtable();
		timer = new Timer(true); //as daemon
		timer.scheduleAtFixedRate(new DeadReferencesRecolector(this),0,10*60*1000); //each 10 minutes
	}
	public synchronized static CacheForImages getInstance(){
		if (imageCache==null)
			imageCache = new CacheForImages();
		return imageCache;
		
	}
	public void addImage(Image image){
		tableImages.put(image.getIdImage(),new CacheForImagesEntry(image));
	}
	public Image getImage(String id){
		if (tableImages.get(id)==null){ //TODO:improve this
			tableImages.remove(id);
			//System.out.println("ERROR: CacheForImages is trying to access a non existing reference:"+id);
			return null;
		}
		((CacheForImagesEntry)tableImages.get(id)).updateLastAccess();
		return ((CacheForImagesEntry)tableImages.get(id)).getImage();
	}
	public void removeImage(String id){
		tableImages.remove(id);
	}
	public Hashtable getTable(){
		return tableImages;
	}
	

}
