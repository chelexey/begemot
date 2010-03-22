//
// Copyright 2010 Anton Lopyrev
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.tokudu.begemot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageCache {
	private static final String LOG_TAG = "ImageCache";
   
    private static File _cacheDir;
    
    private static List<String> _subdirs;
    
    public static void init(Application app) {
    	init(app, null);
    } 
        
    public static void init(Application app, List<String> subdirs) {
    	_cacheDir = app.getCacheDir();
    	_subdirs = subdirs;
    }
    	
    private static File getCacheDirectory() {
    	// If the cache directory hasn't been provided store externally
    	return _cacheDir == null ? IOUtilities.getExternalFile("") : _cacheDir;
    }
    
    private static void ensureCache() throws IOException {
    	if (_subdirs == null) {
    		return;
    	}
    	for(String s : _subdirs) {
	        File userCacheDirectory = new File(getCacheDirectory(), s);
	        if (!userCacheDirectory.exists()) {
	        	userCacheDirectory.mkdirs();
	        	// do not allow media scan
	            new File(userCacheDirectory, ".nomedia").createNewFile();
	        }
    	}
    }    
    
    public static boolean addImageToCache(String pictureId, Bitmap image) {
    	try {
			ensureCache();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Couldn't ensure cache!", e);
			return false;
		}
    	File cacheDirectory = getCacheDirectory();
    	File imageFile = new File(cacheDirectory, pictureId);    	
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);            
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
        	Log.e(LOG_TAG, "Couldn't cache an image: " + pictureId, e);
            return false;
        } finally {
            IOUtilities.closeStream(out);
        }
        Log.i(LOG_TAG, "Cached image: " + pictureId);
        return true;    	
    }
    
    public static Bitmap loadImageFromCache(String imageId) {
    	File cacheDirectory = getCacheDirectory();
    	
    	File imageFile = new File(cacheDirectory, imageId);
    	if (imageFile.exists()) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(imageFile);
                return BitmapFactory.decodeStream(stream, null, null);
            } catch (FileNotFoundException e) {
            	Log.e(LOG_TAG, "Couldn't find image in cache:" + imageId);
            } finally {
                IOUtilities.closeStream(stream);
            }
    	} else {
        	Log.e(LOG_TAG, "Couldn't find image in cache:" + imageId);    		
    	}
    	return null;
    }
}
