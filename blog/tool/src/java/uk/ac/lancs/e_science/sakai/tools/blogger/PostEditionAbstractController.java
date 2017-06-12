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

import uk.ac.lancs.e_science.jsf.components.blogger.IBloggerJSFEditionController;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer; 

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane;

import uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages.CacheForImages;
import uk.ac.lancs.e_science.sakai.tools.blogger.util.JpegTransformer;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.LinkRule;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostElement;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;

public class PostEditionAbstractController extends BloggerController implements IBloggerJSFEditionController {
	
	protected static final String KEYWORDS_MESSAGE="Introduce keywords separated by comma";
	
	protected Post post;
	protected Blogger blogger;
	
    protected String editingText=null;
    protected Image editingImage = null;
    protected String imageDescription=null;
    
    protected File editingFile=null;
    protected String fileDescription=null;

    protected String editingLinkExpression=null;
    protected String editingLinkDescription=null;
    
    protected int currentElementIndex=-1;
    protected String elementTypeUnderEdition = null;
    
    //flags
    protected boolean desactivateSetEditingText=false;
    protected boolean desactivateSetEditingLinkDecription=false;
    protected boolean desactivateSetEditingLinkExpression=false;
    protected boolean showModifyParagraphButton=false;
    protected boolean showModifyImageButton=false;
    protected boolean showModifyLinkButton=false;
    protected boolean showModifyFileButton=false;
    protected boolean isChanged=false;
    protected boolean treatingImageAsFile=false;
    
    
    //tabs
    protected static int INDEX_TEXT=0;
    protected static int INDEX_IMG=1;
    protected static int INDEX_LINK=2;
    protected static int INDEX_FILE=3;
    
    public String doSave(){
        blogger.storePost(post,getCurretUserId(), getCurrentSiteId());
    	resetFields();
        //we have to remove the images from cache
        if (post.getElements()!=null){

        	for (int i=0;i<post.getElements().length;i++){
        		if (post.getElements()[i] instanceof Image){
        			CacheForImages cache = CacheForImages.getInstance();
        			cache.removeImage(((Image)post.getElements()[i]).getIdImage());
        		}
        	}
        }    	
    	return "";
    	
    }
    
    public String doPreview(){
        ServletRequest request = (ServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute("post",post);
    	return "previewPost";
    }
    
    public void setPost(Post post){
    	resetFields();
    	deactivateModifiyButtons();
		isChanged = false;
    	
    	this.post = post;
    	
    }
    public Post getPost(){
        return post;
    }
    

    
    //-----------------------------------------------------------------
    //---------- KEYWORDS ---------------------------------------------
    //-----------------------------------------------------------------
    
    public String getKeywords(){
    	//TODO: internationalitation of KEYWORDS_MESSAGE
    	StringBuffer sb = new StringBuffer("");
    	if (post==null || post.getKeywords()== null || post.getKeywords().length==0)
    		return KEYWORDS_MESSAGE;
    	for (int i=0;i<post.getKeywords().length;i++){
    		sb.append(post.getKeywords()[i]).append(", ");
    	}
    	return sb.toString().substring(0,sb.toString().lastIndexOf(", "));
    }
    public void setKeywords(String keywords){
    	if (keywords.trim().equals("") || (keywords.trim().equals(KEYWORDS_MESSAGE)))
    		post.setKeywords(null);
    	else{
    		post.setKeywords(null); //to start a new list of keywords
    		StringTokenizer st = new StringTokenizer(keywords,",");
    		while (st.hasMoreTokens()){
    			String token = st.nextToken();
    			post.addKeyword(token);
    		}
    	}
    }
	public String getKeywordsMessage(){
		return KEYWORDS_MESSAGE;
	}   
    
    //-----------------------------------------------------------------
    //---------- IMAGE ------------------------------------------------
    //-----------------------------------------------------------------
    public FileItem getImage(){
    	return null;
    }
    
    
    public void setImage(FileItem fileItem){
    	treatingImageAsFile=false;
    	if (fileItem!=null && fileItem.get()!=null && fileItem.get().length>0){
    		byte[] content = fileItem.get();
	    	try{
	    		if (content!=null){
	    			if (content.length<4*1024*1024){ //TODO put that 4 Mb in a property
		    			editingImage = new Image(fileItem.getName(),content);
		    			imageDescription = fileItem.getName();
		    			JpegTransformer transformer = new JpegTransformer(content);
		    			editingImage.setDescription(imageDescription);
		    			editingImage.setThumbnail(transformer.transformJpegFixingLongestDimension(125,0.8f));
		    			editingImage.setWebsize(transformer.transformJpegFixingLongestDimension(300,0.8f));
	    			}else{
	    	    		editingImage = null;
	    	    		imageDescription="";
	    	    		setFile(fileItem.getName(),content);
	    	        	treatingImageAsFile=true;
	    			}
	    		}
	    	} catch (Exception e){
	    		//if we can not treat the Image because of the format, it will be treat as a file
	    		editingImage = null;
	    		imageDescription="";
	    		setFile(fileItem.getName(),content);
	        	treatingImageAsFile=true;
	    	}
    	}
    }	
    public String addImage(){
    	if (treatingImageAsFile){
    		addFile();
    		treatingImageAsFile=false;
    	}
    	else{
	    	resetCurrentElementIndex();
			post.addElement(editingImage);
			CacheForImages imageCache = CacheForImages.getInstance();
			imageCache.addImage(editingImage);
	    	deactivateModifiyButtons();
	    	resetFields();
	    	isChanged = true;
    	}
    	return "";
    }
    
    public String modifyImage(){
		CacheForImages imageCache = CacheForImages.getInstance();
    	if (editingImage!=null){
    		imageCache.removeImage(((Image)post.getElements()[currentElementIndex]).getIdImage());
    		post.replaceElement(editingImage,currentElementIndex);
    		imageCache.addImage(editingImage);
    		editingImage=null;
    	}
    	deactivateModifiyButtons();
    	isChanged = true;
    	return "";    	
    }
    public String getImageDescription(){
    	return imageDescription;
    }
    //-----------------------------------------------------------------
    //---------- FILE -------------------------------------------------
    //-----------------------------------------------------------------

    public FileItem getFile(){
    	return null;
    }   
    
    public void setFile(FileItem i){
    	if (i!=null && i.get()!=null && i.get().length>0){
	    	try{
	    		byte[] content = i.get();
	    		setFile(i.getName(),content);
	    	} catch (Exception e){
	    		e.printStackTrace();
	    	}
    	}
    }	 
    
    private void setFile(String fileName, byte[] content){
    	if (content!=null){
    		editingFile = new File(fileName,content);
    	}
		fileDescription = fileName;
		editingFile.setDescription(fileDescription);
    }
    
    public String addFile(){
    	resetCurrentElementIndex();
		post.addElement(editingFile);
    	deactivateModifiyButtons();
    	resetFields();
    	isChanged = true;
    	return "";
    }
    
    public String modifyFile(){
    	if (editingFile!=null){
    		post.replaceElement(editingFile,currentElementIndex);
    		editingFile=null;
    	}
    	deactivateModifiyButtons();
    	isChanged = true;
    	return "";    	
    }

    public String getFileDescription(){
    	return fileDescription;
    }
    //-----------------------------------------------------------------
    //---------- LINK -------------------------------------------------
    //-----------------------------------------------------------------
    
    
    public String addLink(){
    	HtmlPanelTabbedPane panel = (HtmlPanelTabbedPane)FacesContext.getCurrentInstance().getViewRoot().findComponent("PostForm:tabbedPane");
    	panel.setSelectedIndex(INDEX_LINK);    	

    	resetCurrentElementIndex();
    	if (editingLinkDescription!=null && editingLinkExpression!=null){
    		post.addElement(new LinkRule(editingLinkDescription,editingLinkExpression));
    		editingLinkDescription=null;
    		editingLinkExpression=null;
    	}
    	deactivateModifiyButtons();
    	isChanged = true;
    	return "";  	
    }
    public String modifyLink(){
    	if (editingLinkDescription!=null && editingLinkExpression!=null){
    		post.replaceElement(new LinkRule(editingLinkDescription,editingLinkExpression),currentElementIndex);
    		editingLinkDescription=null;
    		editingLinkExpression=null;
    	}
    	isChanged = true;
    	deactivateModifiyButtons();
    	return "";
    }   
    
    
    public String getLinkDescription(){
    	return editingLinkDescription;
    }

    public void setLinkDescription(String linkDescription ){
    	if (desactivateSetEditingLinkDecription){
    		desactivateSetEditingLinkDecription = false;
    		return;
    	}
    	editingLinkDescription=linkDescription;
    }
    
    public String getLinkExpression(){
    	return editingLinkExpression;
    }
    
    public void setLinkExpression(String linkExpression ){
    	if (desactivateSetEditingLinkExpression){
    		desactivateSetEditingLinkExpression=false;
    		return;
    	}
    	editingLinkExpression = linkExpression;
    }    
    //-----------------------------------------------------------------
    //---------- SHORT TEXT OR ABSTRACT--------------------------------
    //-----------------------------------------------------------------

    public void setShortText(String editingText){
    	post.setShortText(editingText);
    }
    public String getShortText(){
    	return post.getShortText();
    }
    //-----------------------------------------------------------------
    //---------- PARAGRAPH -----------------------------------------
    //-----------------------------------------------------------------

    public String addParagraph(){
    	resetCurrentElementIndex();
    	if (editingText!=null && !editingText.trim().equals(""))
    		post.addElement(new Paragraph(editingText));
    	editingText="";
    	deactivateModifiyButtons();
    	isChanged = true;
    	return "";
    }
    
    public String modifyParagraph(){
    	if (editingText!=null && !editingText.trim().equals(""))
    		post.replaceElement(new Paragraph(editingText),currentElementIndex);
    	editingText="";
    	deactivateModifiyButtons();
    	isChanged = true;
    	return "";
    }  
    public void setEditingText(String editingText){
		if (desactivateSetEditingText)
			desactivateSetEditingText=false;
		else{
			this.editingText = editingText.trim();
		}
    }
    public String getEditingText(){
    	return editingText;
    }
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    public String setCurrentElementIndex(int currentElementIndex) {
    	HtmlPanelTabbedPane panel = (HtmlPanelTabbedPane)FacesContext.getCurrentInstance().getViewRoot().findComponent("PostForm:tabbedPane");
		desactivateSetEditingText=true;
		desactivateSetEditingLinkDecription=true;
		desactivateSetEditingLinkExpression=true;
		this.currentElementIndex = currentElementIndex;
		if (currentElementIndex>=0){
			deactivateModifiyButtons();
			PostElement element = post.getElements()[currentElementIndex];
			if (element instanceof Paragraph){
				editingText = ((Paragraph)element).getText();
				showModifyParagraphButton = true;
				editingImage=null;
		    	panel.setSelectedIndex(INDEX_TEXT);    	
			}
			if (element instanceof Image){
				editingImage = (Image) element;
				imageDescription = editingImage.getDescription();
				showModifyImageButton=true;
				editingText="";
		    	panel.setSelectedIndex(INDEX_IMG);    	
			}
			if (element instanceof LinkRule){
				editingLinkDescription = ((LinkRule) element).getDescription();
				editingLinkExpression = ((LinkRule) element).getLinkExpression();
				showModifyLinkButton = true;
				editingText="";
		    	panel.setSelectedIndex(INDEX_LINK);    	
				
			}
			if (element instanceof File){
				editingFile = (File) element;
			    fileDescription=editingFile.getDescription();
				showModifyFileButton = true;
				editingText="";
		    	panel.setSelectedIndex(INDEX_FILE);    	
			}
		}
    	return "";
			
		
	}
    public void removeElement(int index){
    	
    	PostElement element= post.getElements()[index];
    	if (element instanceof Image){
    		CacheForImages cache = CacheForImages.getInstance();
    		cache.removeImage(((Image)element).getIdImage());
    	}
    	post.removeElement(index);
    	deactivateModifiyButtons();
    	isChanged = true;
    }

	public int getCurrentElementIndex() {
		return currentElementIndex;
	}
	public boolean getShowModifyParagraphButton() {
		return showModifyParagraphButton;
	}

	public boolean getShowModifyImageButton() {
		return showModifyImageButton;
	}
	public boolean getShowModifyLinkButton() {
		return showModifyLinkButton;
	}	
	public boolean getShowModifyFileButton() {
		return showModifyFileButton;
	}	
	public void setElementTypeUnderEdition(String typeName){
		elementTypeUnderEdition = typeName;
	}
	public void upElement(int index){
		if (index==0)
			return;
		PostElement e1 = post.getElements()[index-1];
		PostElement e2 = post.getElements()[index];
		post.replaceElement(e1,index);
		post.replaceElement(e2,index-1);
    	deactivateModifiyButtons();
    	isChanged = true;

		
	}
	public void downElement(int index){
		if (index==post.getElements().length)
			return;
		PostElement e1 = post.getElements()[index];
		PostElement e2 = post.getElements()[index+1];
		post.replaceElement(e1,index+1);
		post.replaceElement(e2,index);
    	deactivateModifiyButtons();
    	isChanged = true;


	}
	



	
    public void setActivePane(int activePane){
    	
    }
    public int getActivePane(){
    	return 0;
    }
	private void resetCurrentElementIndex(){
		this.currentElementIndex = -1;
	}
	protected void resetFields(){
		editingText="";
		editingImage=null;
		editingLinkDescription = null;
		editingLinkExpression = null;
		editingFile=null;
		fileDescription=null;
	}	
	
	private void deactivateModifiyButtons(){
		showModifyImageButton = false;
		showModifyParagraphButton = false;
		showModifyLinkButton = false;
		showModifyFileButton = false;
	}	
	
    public List getVisibilityList(){

        ArrayList result = new ArrayList();
        result.add(new SelectItem(new Integer(State.PRIVATE),"PRIVATE"));
        result.add(new SelectItem(new Integer(State.SITE),"SITE"));
        //result.add(new SelectItem(new Integer(State.PUBLIC),"PUBLIC"));
        return result;
    }
    
    public boolean getIsChanged(){
    	return isChanged; //if this value is true, that means we can be modifiying a paragraph
    }
    

    
}

