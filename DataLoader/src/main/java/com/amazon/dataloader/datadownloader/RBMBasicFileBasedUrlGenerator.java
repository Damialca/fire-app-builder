/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.dataloader.datadownloader;

import android.content.Context;
import android.util.Log;

import com.amazon.android.utils.FileHelper;
import com.amazon.android.utils.JsonHelper;
import com.amazon.dataloader.R;
import com.amazon.utils.ObjectVerification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This URL generator reads URLs from a JSON file and sends the URL at the required index.
 * The URL file format must be { "{@link #URLS}" : [ "url1", "url2"] }. The {@link #getUrl} method
 * expects a key {@link #URL_FILE} in the params that links to the path
 * of the URL file that is located in the assets directory. It also expects another key {@link
 * #URL_INDEX} that gives the value of the required index
 * as a string.
 */
public class RBMBasicFileBasedUrlGenerator extends AUrlGenerator {

    /**
     * Keys.
     */
    private static final String URLS = "urls";
    private static final String URL_INDEX = "url_index";
    private static final String URL_FILE = "url_file";

    /**
     * Debug tag.
     */
    private static final String TAG = RBMBasicFileBasedUrlGenerator.class.getName();

    /**
     * {@inheritDoc}
     *
     * Constructs a {@link BasicTokenBasedUrlGenerator}.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public RBMBasicFileBasedUrlGenerator(Context context) throws ObjectCreatorException {

        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return context.getString(R.string.basic_file_based_url_generator_config_file_path);
    }

    /**
     * Provides a concrete implementation of {@link RBMBasicFileBasedUrlGenerator}.
     *
     * @param context The context to extract application context from.
     * @return Concrete implementation of the UrlGenerator.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public static AUrlGenerator createInstance(Context context) throws ObjectCreatorException {

        return new RBMBasicFileBasedUrlGenerator(context);
    }

    /**
     * {@inheritDoc}
     *
     * It reads a file that contains the URLs in JSON format and returns the Nth URL from this
     * file. The method expects a key {@link #URL_FILE} in the params that links to the path of the
     * URL file located in the assets directory. The configuration file is searched if params
     * doesn't have this value. It also expects another key {@link #URL_INDEX} in params that should
     * give the value of the index as String.
     *
     * @param params Map that has the {@link #URL_INDEX} key with the value as the desired index (in
     *               string format).
     *               It can optionally also have the URL file name with key {@link #URL_FILE}
     */
    @Override
    public String getUrl(Map params) throws UrlGeneratorException {

        params = ObjectVerification.notNull(params, "params map cannot be null");
        try {
            String url_file_path = getKey(params, URL_FILE);
            String url_file_content = FileHelper.readFile(mContext,
                                                          url_file_path);
            // Converting the data into a map to fetch the URLS key
            Map urlMap = JsonHelper.stringToMap(JsonHelper.escapeComments
                    (url_file_content));
            List<String> urlList = (List<String>) urlMap.get(URLS);

            // Fetch the index at which the desired URL resides
            int urlIndex = Integer.parseInt((String) params.get(URL_INDEX));
            // Return the URL at the desired index

            return urlList.get(urlIndex);
        }
        catch (Exception e) {
            Log.e(TAG, "Could not read url at index " + params.get(URL_INDEX) + " in file " +
                    params.get(URL_FILE), e);
            throw new UrlGeneratorException("Could not read url at index " + params.get
                    (URL_INDEX) + " in file " + params.get(URL_FILE), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Modifies the params to extract array of indexes for each URL and call getUrl with each index.
     * It expects a key {@link #URL_INDEX} in params that should
     * give the value of the indexes as String array.
     *
     * @param params Map that has the {@link #URL_INDEX} key with the value as the desired index (in
     *               string format).
     *               It can optionally also have the URL file name with key {@link #URL_FILE}
     */
    @Override
    public List<String> getUrlArray(Map params) throws UrlGeneratorException {

        //  RBM 12.29.2017
        //  --------------
        //  Obtain urls from array of indexes
        //  The URL INDEX field of the Data Loader Recipes for each
        //  category will be an array of strings.
        //  "url_index": ["0","10"]
        params = ObjectVerification.notNull(params, "params map cannot be null");
        try {
            String url_file_path = getKey(params, URL_FILE);
            String url_file_content = FileHelper.readFile(mContext,
                    url_file_path);
            // Converting the data into a map to fetch the URLS key
            Map urlMap = JsonHelper.stringToMap(JsonHelper.escapeComments
                    (url_file_content));
            List<String> urlList = (List<String>) urlMap.get(URLS);
            List<String> urlIndexList = (List<String>) params.get(URL_INDEX);
            List<String> urlListResult = new ArrayList<String>();

            for (String index : urlIndexList) {
                // Fetch the URL at the desired index
                int urlIndex = Integer.parseInt((String)index);
                urlListResult.add(urlList.get(urlIndex));
            }
            // Return the URL at the desired index
            return urlListResult;
        }
        catch (Exception e) {
            Log.e(TAG, "Could not read url at index " + params.get(URL_INDEX) + " in file " +
                    params.get(URL_FILE), e);
            throw new UrlGeneratorException("Could not read url at index " + params.get
                    (URL_INDEX) + " in file " + params.get(URL_FILE), e);
        }

//        List<String> urlIndexList = (List<String>) params.get(URL_INDEX);
//        List<String> urlListResult = new ArrayList<String>();
//
//        for (String index : urlIndexList) {
//            // Fetch the URL at the desired index
//            Log.e(TAG,"RBM - url index Before " + params.get(URL_INDEX) + " - " + getKey(params, URL_INDEX));
//            params.put(URL_INDEX,index);
//            Log.e(TAG,"RBM - url index After " + params.get(URL_INDEX) + " - " + getKey(params, URL_INDEX));
//
//            urlListResult.add(getUrl(params));
//        }

//        return urlListResult;
    }
}
