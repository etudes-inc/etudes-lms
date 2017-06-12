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
package uk.ac.lancs.e_science.sakai.tools.blogger.util;

import java.awt.Graphics2D;

import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class JpegTransformer {

	BufferedImage image;
	Double imageHeight;
	Double imageWidth;
	public JpegTransformer(byte[] image) throws JpegTransformerException{
		try{
			this.image = ImageIO.read(new ByteArrayInputStream(image));
		    imageHeight= new Double(this.image.getHeight());
		    imageWidth= new Double(this.image.getWidth());	

		} catch (IOException e){
			throw new JpegTransformerException("\n{JpegTransformer.JpegFileTransformer()}[IOException]"+e.getMessage());
		}
	}
	public JpegTransformer(File ficheroOrigen) throws JpegTransformerException{
		try{
			image = ImageIO.read(ficheroOrigen);
		    imageHeight= new Double(image.getHeight());
		    imageWidth= new Double(image.getWidth());	

		} catch (IOException e){
			throw new JpegTransformerException("\n{JpegTransformer.JpegFileTransformer()}[IOException]"+e.getMessage());
		}
	}
	
	public float getLongestDimension(){
		if (imageWidth.floatValue()> imageHeight.floatValue())
			return  imageWidth.floatValue();
		return  imageHeight.floatValue();

	}

	
	public float getWidth(){
		return imageWidth.floatValue();
	}
	
	
	public float getHeight(){
		return imageHeight.floatValue();
	}
	/**
	 * Takes a Jpeg file transforming it in a new one using a scale factor and compression quality
	 * The size of the new image will be the original width multiplied by the scale factor. The same is applied to image's height;
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public void transformJpeg(File targetFile, float scaleFactor, float quality) throws JpegTransformerException{
		try{
		    Float height= new Float(image.getHeight());
		    Float width= new Float(image.getWidth());	
		    
		    int scaleWidth = new Float(width.floatValue()*scaleFactor).intValue();
			int heightScale = new Float(height.floatValue()*scaleFactor).intValue();
			
			transformJpeg(targetFile,scaleWidth, heightScale, quality);
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegFile()}[Exception]"+e.getMessage());
		}
	}
	/**
	 * Takes a Jpeg file transforming it in a new one fixing the width or height (the bigest one), keeping the proportions and using a compression queality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public void transformJpegFixingLongestDimension(File targetFile, int longestDimension, float quality) throws JpegTransformerException{
		if (imageHeight.doubleValue() > imageWidth.doubleValue())
			transformJpegFixingHeight(targetFile,longestDimension,quality);
		else
			transformJpegFixingWidth(targetFile,longestDimension,quality);
	}
	/**
	 * Takes a Jpeg file transforming it in a new one fixing the width or height (the bigest one), keeping the proportions and using a compression queality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public byte[] transformJpegFixingLongestDimension(int longestDimension, float quality) throws JpegTransformerException{
		if (imageHeight.doubleValue() > imageWidth.doubleValue())
			return transformJpegFixingHeight(longestDimension,quality);
		else
			return transformJpegFixingWidth(longestDimension,quality);
	}	

	/**
	 * Takes a Jpeg file transforming it in a new one fixing the width, keeping the proportions and using a compression queality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public void transformJpegFixingWidth(File targetFile, int fixedWidth, float quality) throws JpegTransformerException{
		try{
		    float scaleFactor = fixedWidth/imageWidth.floatValue();
		    
		    int scaleWidth = new Float(imageWidth.floatValue()*scaleFactor).intValue();
			int scaleHeight = new Float(imageHeight.floatValue()*scaleFactor).intValue();
			
			transformJpeg(targetFile,scaleWidth, scaleHeight, quality);
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegFileFixingWidth()}[Exception]"+e.getMessage());
		}
	}
	/**
	 * Takes a Jpeg file transforming it in a new one fixing the width, keeping the proportions and using a compression queality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public byte[] transformJpegFixingWidth(int fixedWidth, float quality) throws JpegTransformerException{
		try{
		    float scaleFactor = fixedWidth/imageWidth.floatValue();
		    
		    int scaleWidth = new Float(imageWidth.floatValue()*scaleFactor).intValue();
			int scaleHeight = new Float(imageHeight.floatValue()*scaleFactor).intValue();
			
			return transformJpegImage(scaleWidth, scaleHeight, quality);
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegFileFixingWidth()}[Exception]"+e.getMessage());
		}
	}

	/**
	 * Takes a Jpeg file transforming it in a new one fixing the height, keeping the proportions and using a compression queality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public void transformJpegFixingHeight(File targetFile, int fixedHeight, float quality) throws JpegTransformerException{
		try{
		    
		    float scaleFactor = fixedHeight/imageHeight.floatValue();
		    
		    int scaleWidth = new Float(imageWidth.floatValue()*scaleFactor).intValue();
			int scaleHeight = new Float(imageHeight.floatValue()*scaleFactor).intValue();
			
			transformJpeg(targetFile,scaleWidth, scaleHeight, quality);
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegFileFixingHeight()}[Exception]"+e.getMessage());
		}
	}	
	/**
	 * Takes a Jpeg file transforming it in a new one fixing the height, keeping the proportions and using a compression queality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public byte[] transformJpegFixingHeight(int fixedHeight, float quality) throws JpegTransformerException{
		try{
		    
		    float scaleFactor = fixedHeight/imageHeight.floatValue();
		    
		    int scaleWidth = new Float(imageWidth.floatValue()*scaleFactor).intValue();
			int scaleHeight = new Float(imageHeight.floatValue()*scaleFactor).intValue();
			
			return transformJpegImage(scaleWidth, scaleHeight, quality);
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegFileFixingHeight()}[Exception]"+e.getMessage());
		}
	}	

	/**
	 * Takes a Jpeg file transforming it to adjust to a width and height and compression quality
	 * The compression quality has to be between 1 and 0, being 1 the highest quality and 0 the lowest.
	 */
	public void transformJpeg(File targetFile, int width, int height, float quality) throws JpegTransformerException{
		try{
		    transformJpegImage(targetFile,width,height,quality);
		    
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegFile()}[Exception]"+e.getMessage());
		}
	}
	public byte[] transformJpegImage(int width, int height, float quality) throws JpegTransformerException{
		try{
		    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
		    Graphics2D graphics2D = newImage.createGraphics();
		    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		    graphics2D.drawImage(image, 0, 0, width, height, null);	
		   
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(newImage);		    
		    param.setQuality(quality,true);
			
		    encoder.encode(newImage,param);
		    out.flush();
		    return out.toByteArray();
		} catch (FileNotFoundException e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegImage()}[FileNotFoundException]"+e.getMessage());
			
		} catch (IOException e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegImage()}[IOException]"+e.getMessage());
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegImage()}[Exception]"+e.getMessage());
		}
	}

	private  void transformJpegImage(File targetFile, int width, int height, float quality) throws JpegTransformerException{
		try{
		    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
		    Graphics2D graphics2D = newImage.createGraphics();
		    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		    graphics2D.drawImage(image, 0, 0, width, height, null);	
		   
		    OutputStream out = new FileOutputStream( targetFile);
		    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(newImage);		    
		    param.setQuality(quality,true);
			
		    encoder.encode(newImage,param);	
		} catch (FileNotFoundException e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegImage()}[FileNotFoundException]"+e.getMessage());
			
		} catch (IOException e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegImage()}[IOException]"+e.getMessage());
		} catch (Exception e){
			throw new JpegTransformerException("\n{JpegTransformer.transformJpegImage()}[Exception]"+e.getMessage());
		}
	}
}
