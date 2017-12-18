/*
 * This file is NOT a part of Moodle - http://moodle.org/
 *
 * This client for Moodle 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package artifact;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * REST MOODLE Client
 * It's very basic. You'll have to write the JavaObject2POST code.
 *
 * @author 
 */
public class WebServiceMoodle {

    /**
     * Do a REST call to Moodle. Result are displayed in the console log.
     * @param args the command line arguments
     */
    public void cria_grupos(String nomeGrupo, int idCourse) throws ProtocolException, IOException {

        /// NEED TO BE CHANGED
        String token = "1d8f06e35815f80def22a1a0932ee5c4";
        String domainName = "http://posiate.inf.ufsc.br/moodlececilia";

        /// REST RETURNED VALUES FORMAT
        String restformat = "xml"; //Also possible in Moodle 2.2 and later: 'json'
                                   //Setting it to 'json' will fail all calls on earlier Moodle version
        if (restformat.equals("json")) {
            restformat = "&moodlewsrestformat=" + restformat;
        } else {
            restformat = "";
        }

        /// PARAMETERS - NEED TO BE CHANGED IF YOU CALL A DIFFERENT FUNCTION
        String functionName = "core_group_create_groups";
        String urlParameters =
        "groups[0][courseid]=" + URLEncoder.encode(""+idCourse, "UTF-8") +
        "&groups[0][name]=" + URLEncoder.encode(nomeGrupo, "UTF-8") +
        "&groups[0][description]=" + URLEncoder.encode("descrição", "UTF-8") +
        "&groups[0][descriptionformat]=" + URLEncoder.encode("1", "UTF-8");

        /// REST CALL

        // Send request
        String serverurl = domainName + "/webservice/rest/server.php" + "?wstoken=" + token + "&wsfunction=" + functionName;
        HttpURLConnection con = (HttpURLConnection) new URL(serverurl).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type",
           "application/x-www-form-urlencoded");
        con.setRequestProperty("Content-Language", "en-US");
        con.setDoOutput(true);
        con.setUseCaches (false);
        con.setDoInput(true);
        DataOutputStream wr = new DataOutputStream (
                  con.getOutputStream ());
        wr.writeBytes (urlParameters);
        wr.flush ();
        wr.close ();

        //Get Response
        InputStream is =con.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder response = new StringBuilder();
        while((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        //System.out.println("RESPOSTA "+response.toString());
    }
}                            
