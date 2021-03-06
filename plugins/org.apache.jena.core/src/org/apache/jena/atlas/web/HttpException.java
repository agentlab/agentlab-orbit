/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.web;

/**
 * Class of HTTP Exceptions from Atlas code
 * 
 */
public class HttpException extends RuntimeException {
    private static final long serialVersionUID = -7224224620679594095L;
    private int responseCode = -1;

    public HttpException(int responseCode, String statusLine) {
        super(responseCode + " - " + statusLine);
        this.responseCode = responseCode;
    }
    
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public HttpException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Gets the response code, may be -1 if unknown
     * @return Response Code if known, -1 otherwise
     */
    public int getResponseCode() {
        return this.responseCode;
    }
}
