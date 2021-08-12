package com.planview.lkutility;

import java.util.Base64;

/**
 * Fields required in the Config page of the XLSX spreadsheet.
 * 
 * All fieldnames must be lowerCamelcase and alphabetical (no wierd-shit characters) 
 */

public class Configuration {
    public String url;  //Must be first in this object.
    public String username;
    public String password;
    public String apiKey;
    public String boardId;
    public String hash() {
        return Base64.getEncoder().encodeToString(
            (url+username+password+apiKey.toString()).getBytes()
            );
    }
}

