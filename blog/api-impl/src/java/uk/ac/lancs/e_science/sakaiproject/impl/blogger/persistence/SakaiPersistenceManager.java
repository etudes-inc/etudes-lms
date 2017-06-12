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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence;



import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import java.sql.*;

import org.sakaiproject.db.api.SqlService;



import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.xml.XMLToPost;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util.HiperSonicGenerator;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util.ISQLGenerator;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util.MySQLGenerator;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util.SQLGenerator;

public class SakaiPersistenceManager{
    private SqlService sqlService;
    ISQLGenerator sqlGenerator;
    public SakaiPersistenceManager() throws PersistenceException{
        sqlService = org.sakaiproject.db.cover.SqlService.getInstance();
        String vendor = sqlService.getVendor();
        //TODO load the proper class using reflection. We can use a named based system to locate the correct SQLGenerator
        if (vendor.equals("mysql"))
        	sqlGenerator = new MySQLGenerator();
        else if (vendor.equals("oracle"))
            sqlGenerator = new SQLGenerator();
        else if (vendor.equals("hsqldb"))
        	sqlGenerator = new HiperSonicGenerator();
        else
        	throw new PersistenceException("Unknown database vendor:"+vendor);

    }

    public void storePost(Post post, String siteId) throws PersistenceException{
        Connection connection = getConnection();
        try{
            Collection sqlStatements;
            if (!existPost(post.getOID())){
                sqlStatements = sqlGenerator.getInsertStatementsForPost(post, siteId, connection);
            	executeSQL(sqlStatements, connection);
            }
            else { //delete and insert again. this is less efficient but simplier
            	Post originalPost = getPost(post.getOID());
           		sqlStatements = sqlGenerator.getDeleteStatementsForPostExcludingImagesAndFiles(post.getOID());
           		executeSQL(sqlStatements, connection);
               	try{
            		sqlStatements = sqlGenerator.getInsertStatementsForPostExcludingImagesAndFiles(post, siteId);
            		executeSQL(sqlStatements, connection);
            	} catch (Exception e){
            		sqlStatements = sqlGenerator.getInsertStatementsForPostExcludingImagesAndFiles(originalPost, siteId);
            		//this can happen when the post has odd characteres that we did't deal with them. But we tried our best!!!
            	}
                
                //Now... Images ... We need be more efficient.
            	Collection imagesIdInDb = getIdImages(post);
            	Iterator itImagesIdInDb = imagesIdInDb.iterator();
            	while (itImagesIdInDb.hasNext()){
            		String imageId = (String)itImagesIdInDb.next();
            		if (!post.hasImage(imageId))
            			executeSQL(sqlGenerator.getDeleteStatementForImage(imageId),connection);
            	}
            	if (post.getImages()!=null){
	        		ArrayList imagesToInserting = new ArrayList();
	            	for (int i=0;i<post.getImages().length;i++){
	            		if (!imagesIdInDb.contains(post.getImages()[i].getIdImage()))
	            			imagesToInserting.add(post.getImages()[i]);
	            	}
	        		executeSQL(sqlGenerator.getInsertStatementsForImages((Image[])imagesToInserting.toArray(new Image[0]),post.getOID(),connection),connection);
            	}
                //Now... Files ... We need be more efficient.
            	Collection filesIdInDb = getIdFiles(post);
            	Iterator itFilesIdInDb = filesIdInDb.iterator();
            	while (itFilesIdInDb.hasNext()){
            		String fileId = (String)itFilesIdInDb.next();
            		if (!post.hasFile(fileId))
            			executeSQL(sqlGenerator.getDeleteStatementForFile(fileId),connection);
            	}
            	if (post.getFiles()!=null){
            		ArrayList filesToInserting = new ArrayList();
            		for (int i=0;i<post.getFiles().length;i++){
            			if (!filesIdInDb.contains(post.getFiles()[i].getIdFile()))
            				filesToInserting.add(post.getFiles()[i]);
            		}           	
            		executeSQL(sqlGenerator.getInsertStatementsForFiles((File[])filesToInserting.toArray(new File[0]),post.getOID(),connection),connection);
            	}
            }
        } catch (SQLException e){
        	e.printStackTrace();
        } finally{
            releaseConnection(connection);
        }
    }
    


    public void deletePost(String postId) throws PersistenceException{
        Connection connection = getConnection();
        try{
            Collection sqlStatements;
            sqlStatements = sqlGenerator.getDeleteStatementsForPost(postId);

            executeSQL(sqlStatements, connection);
        } finally{
            releaseConnection(connection);
        }
    }

    public List getPosts(QueryBean query, String siteId) throws PersistenceException{
        Connection connection = getConnection();
        try{
            String statement = sqlGenerator.getSelectStatementForQuery(query, siteId);
            ResultSet rs = executeQuerySQL(statement, connection);
            List result = transformResultSetInPostCollection(rs, false, false);
            return result;
        } finally{
            releaseConnection(connection);
        }
    }
    
    public Post getPost(String postId) throws PersistenceException{
        Connection connection = getConnection();
        try{
            String statement = sqlGenerator.getSelectPost(postId);
            ResultSet rs = executeQuerySQL(statement, connection);
            List result = transformResultSetInPostCollection(rs,true,true); //TODO: Do we need load the files?
            if (result.size()==0)
            	throw new PersistenceException("getPost: Unable to find post with id:"+postId);
            if (result.size()>1)
            	throw new PersistenceException("getPost: there are more than one post with id:"+postId);
            return (Post)result.get(0);
        } finally{
            releaseConnection(connection);
        }
    }
    
    public List getAllPost(String siteId) throws PersistenceException{
        Connection connection = getConnection();
        try{
            ResultSet rs = executeQuerySQL(sqlGenerator.getSelectAllPost(siteId), connection);
            List result = transformResultSetInPostCollection(rs, false, false);
            return result;
        } finally{
            releaseConnection(connection);
        }

    }

    public boolean existPost(String OID) throws PersistenceException{
        Connection connection = getConnection();
        try{
            try{
                ResultSet rs = executeQuerySQL(sqlGenerator.getSelectPost(OID), connection);
                return (rs.next());
            } catch (SQLException e){
                throw new PersistenceException();
            }

        } finally{
            releaseConnection(connection);
        }

    }
    /**
     * 
     * @param post
     * @return Collection with the image's identifier currently in the database that belows to the post
     * @throws PersistenceException
     */
    public Collection getIdImages(Post post) throws PersistenceException{
        ArrayList result = new ArrayList();
    	Connection connection = getConnection();
        try{
            try{
                ResultSet rs = executeQuerySQL(sqlGenerator.getSelectIdImagesFromPost(post), connection);
                while (rs.next()){
                	result.add(rs.getString(1));
                }
                return result;
            } catch (SQLException e){
                throw new PersistenceException();
            }

        } finally{
            releaseConnection(connection);
        }
    }
    public Image getImage(String imageId, int size) throws PersistenceException{
        Connection connection = getConnection();
        Image image = new Image();
        image.setIdImage(imageId);
        try{
            try{
                ResultSet rs = executeQuerySQL(sqlGenerator.getSelectImage(imageId), connection);
                if (rs==null)
                	return null;
                if (!rs.next())
                	return null;
                //we only need recover the content
                if (size==Blogger.ORIGINAL||size==Blogger.ALL)
                	image.setContent(rs.getBytes(3));
                if (size==Blogger.THUMBNAIL||size==Blogger.ALL)
                	image.setThumbnail(rs.getBytes(4));
                if (size==Blogger.WEB||size==Blogger.ALL)
                	image.setWebsize(rs.getBytes(5));
                return image;
            } catch (SQLException e){
                throw new PersistenceException();
            }

        } finally{
            releaseConnection(connection);
        }
    }
    /**
     * 
     * @param post
     * @return Collection with the file's identifier currently in the database that belows to the post
     * @throws PersistenceException
     */
    public Collection getIdFiles(Post post) throws PersistenceException{
        ArrayList result = new ArrayList();
    	Connection connection = getConnection();
        try{
            try{
                ResultSet rs = executeQuerySQL(sqlGenerator.getSelectIdFilesFromPost(post), connection);
                while (rs.next()){
                	result.add(rs.getString(1));
                }
                return result;
            } catch (SQLException e){
                throw new PersistenceException();
            }

        } finally{
            releaseConnection(connection);
        }
    }
    public File getFile(String fileId) throws PersistenceException{
        Connection connection = getConnection();
        File file = new File();
        file.setIdFile(fileId);
        try{
            try{
                ResultSet rs = executeQuerySQL(sqlGenerator.getSelectFile(fileId), connection);
                if (rs==null)
                	return null;
                if (!rs.next())
                	return null;
                //we only need recover the content
                file.setContent(rs.getBytes(3));
                return file;
            } catch (SQLException e){
                throw new PersistenceException();
            }

        } finally{
            releaseConnection(connection);
        }
    }


    private ResultSet executeQuerySQL(String sql, Connection connection) throws PersistenceException{
        try{
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e){
            throw new PersistenceException();
        }
    }
    private void executeSQL(String sql, Connection connection) throws PersistenceException{
    	Collection sqlList = new ArrayList();
    	sqlList.add(sql);
    	executeSQL(sqlList, connection);
    }

    private void executeSQL(Collection sql, Connection connection) throws PersistenceException{
        try{
            Iterator it = sql.iterator();
            boolean autocommit = connection.getAutoCommit();
            while (it.hasNext()){
            	Object sentence = it.next();
            	if (sentence instanceof String){
            		String sqlSentence = (String)sentence;
            		Statement statement = connection.createStatement();
            		if (sqlSentence.indexOf("SELECT")==0)
            			statement.executeQuery(sqlSentence);
            		else{
            			connection.setAutoCommit(true);
            			statement.executeUpdate(sqlSentence);
            			connection.setAutoCommit(autocommit);
            		}
            	} else if (sentence instanceof PreparedStatement){ 
            		try{
            			PreparedStatement statement = (PreparedStatement)sentence;
            			//we use preparede statements to insert or update data with BLOB
            			connection.setAutoCommit(true); 
            			statement.executeUpdate();
            			connection.setAutoCommit(autocommit);
            		} catch (SQLException e){
            			System.out.println("Exception in Prepared statement");
                    	System.out.println("SQLException:"+e.getMessage());
            			
            		}
            	}

            }
        } catch (SQLException e){
        	//System.out.println("SQLException:"+e.getMessage());
        	
            throw new PersistenceException();
        }
    }

    private List transformResultSetInPostCollection(ResultSet rs, boolean loadImages, boolean loadFiles) throws PersistenceException{
        ArrayList result = new ArrayList();
        if (rs==null)
            return result;
        XMLToPost xmlToPost = new XMLToPost();
        try{
            while (rs.next()){
                String xml = rs.getString(ISQLGenerator.XMLCOLUMN);
                Post post = xmlToPost.convertXMLInPost(xml.replaceAll(SQLGenerator.APOSTROFE,"'")); 
                if (post!=null){ //post can be null if was imposible to parse the xml document. This can happend when the xml has a invalid caracter like (0x1a) 
                	/*
                	if (loadImages)
                		recoverImages(post);
                	if (loadFiles)
                    	recoverFiles(post);
                	*/
                    result.add(post);
                }
           }
        } catch (SQLException e){
            throw new PersistenceException();
        }
        return result;
    }
    
    //this method is used when we need recover a post. We don't need recover all sizes of images. 
    /*
    private void recoverImages(Post post) throws PersistenceException{
    	if (post.getElements()!=null){
    		for (PostElement element :post.getElements()){
    			if (element instanceof Image){
    				Image imageInDB = getImage(((Image)element).getIdImage());
    				((Image)element).setContent(imageInDB.getContent());
    				((Image)element).setThumbnail(imageInDB.getThumbnail());
    				((Image)element).setWebsize(imageInDB.getWebsize());
    			}
    		}
    	}
    }

    private void recoverFiles(Post post) throws PersistenceException{
    	if (post.getElements()!=null){
    		for (PostElement element :post.getElements()){
    			if (element instanceof File){
    				File fileInDB = getFile(((File)element).getIdFile());
    				((File)element).setContent(fileInDB.getContent());
    			}
    		}
    	}
    }    
    */
    private void releaseConnection(Connection connection){
    	try{
    		sqlService.returnConnection(connection);
    	} catch (Exception e){
    		//we did our best...
    	}
    }

    private Connection getConnection() throws PersistenceException{
         try{
            return sqlService.borrowConnection();
        } catch (SQLException e){
            throw new PersistenceException();
        }
    }

    public void initRepository() throws PersistenceException{
        Connection connection = getConnection();
        try{
            Collection statements = sqlGenerator.getCreateStatementsForPost();
            executeSQL(statements,connection);
        }  catch (Exception e){
        	//e.printStackTrace();
        }
        finally{
            releaseConnection(connection);
        }

    }

}
