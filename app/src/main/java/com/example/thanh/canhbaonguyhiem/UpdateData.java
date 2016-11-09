package com.example.thanh.canhbaonguyhiem;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Thanh on 12/6/2015.
 */
public class UpdateData extends AsyncTask<String, Integer, ArrayList<ArrayList>> {

    boolean isListViTriNguyHiemUpdated = false;
    boolean isListViTriUnTacUpdated = false;
    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected ArrayList<ArrayList> doInBackground(String... params) {
        ArrayList<ArrayList> a = new ArrayList<>();
        ArrayList<ViTriNguyHiem> ListViTriNguyHiem = updateDataViTriNguyHiem();
        ArrayList<ViTriNguyHiem> ListViTriUnTac = updateDataViTriUnTac();
        if (isListViTriNguyHiemUpdated){
            a.add(ListViTriNguyHiem);
        }
        else{
            a.add(null);
        }
        if (isListViTriUnTacUpdated) {
            a.add(ListViTriUnTac);
        } else{
            a.add(null);
        }
        return a;
    }

    @Override
    protected void onPostExecute(ArrayList<ArrayList> result) {
        MapsActivity.Data = result;
    }

    private ArrayList updateDataViTriNguyHiem(){
        ArrayList<ViTriNguyHiem> ListViTriNguyHiem = new ArrayList<>();
        Document doc = null;
        try {
            android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
            android.os.StrictMode.setThreadPolicy(policy);

            URL urlConnection = new URL(MapsActivity.baseurl + "dataNguyHiem.xml");
            // tao connection
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Log.i("update", "connection connected !! ");
            //Đọc dữ liệu
            InputStream input = connection.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.
                    newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            Log.i("update", "document created !! ");
            // get hash code here
            Node hashCode = doc.getElementsByTagName("HashCode").item(0);
            Log.i("update", "hash code node created");
            String newHashCodeNguyHiem = hashCode.getTextContent();
            Log.i("update", newHashCodeNguyHiem);
            if (!newHashCodeNguyHiem.toString().equals(MapsActivity.hashCodeNguyHiem.toString())){
                // ok, start to update data
                ListViTriNguyHiem.clear();
                // loop through each item
                NodeList items = doc.getElementsByTagName("ViTriNguyHiem");
                Node n = items.item(0);
                NodeList nodeList = n.getChildNodes();
                //Log.i("intersection", "node list length = : " + nodeList.getLength());
                for (int i = 0; i < nodeList.getLength(); i++)
                {
                    Node n1 = nodeList.item(i);
                    //Log.i("name", "node name = " + n1.getNodeName());
                    NodeList nodelist = n1.getChildNodes();
                    String name = "";
                    double lat = 0, lon = 0;
                    int mucdo = 0;
                    boolean active = false;
                    for (int j = 0; j < nodelist.getLength(); j++){
                        String nodename = nodelist.item(j).getNodeName();

                        if (nodename.equalsIgnoreCase("Name")){
                            name = nodelist.item(j).getTextContent();
                        }
                        if (nodename.equalsIgnoreCase("Lat")){
                            lat = Double.parseDouble(nodelist.item(j).getTextContent());
                        }
                        if (nodename.equalsIgnoreCase("Lon")){
                            lon = Double.parseDouble(nodelist.item(j).getTextContent());
                        }
                        if (nodename.equalsIgnoreCase("MucDo")){
                            mucdo = Integer.parseInt(nodelist.item(j).getTextContent());
                        }
                        if (nodename.equalsIgnoreCase("Active")){
                            active = ((nodelist.item(j).getTextContent().equalsIgnoreCase("true")));
                        }
                    }

                    if (active){
                    ViTriNguyHiem vitri = new ViTriNguyHiem(new LatLng(lat,lon),name,0,mucdo,1);
                    ListViTriNguyHiem.add(vitri);}
                }
                isListViTriNguyHiemUpdated = true;
            }
            MapsActivity.hashCodeNguyHiem = newHashCodeNguyHiem;
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return ListViTriNguyHiem;
    }

    private ArrayList updateDataViTriUnTac(){
        ArrayList<ViTriNguyHiem> ListViTriKetXe = new ArrayList<>();
        Document doc = null;
        try {
            android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
            android.os.StrictMode.setThreadPolicy(policy);

            URL urlConnection = new URL(MapsActivity.baseurl + "dataUnTac.xml");
            // tao connection
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Log.i("update", "connection connected !! ");
            //Đọc dữ liệu
            InputStream input = connection.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.
                    newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            Log.i("update", "document created !! ");
            // get hash code here
            Node hashCode = doc.getElementsByTagName("HashCode").item(0);
            Log.i("update", "hash code node created");
            String newHashCodeUnTac = hashCode.getTextContent();
            Log.i("update", newHashCodeUnTac);
            if (!newHashCodeUnTac.toString().equals(MapsActivity.hashCodeUnTac.toString())){
                // ok, start to update data
                ListViTriKetXe.clear();
                // loop through each item
                NodeList items = doc.getElementsByTagName("ViTriUnTac");
                Node n = items.item(0);
                NodeList nodeList = n.getChildNodes();
                //Log.i("intersection", "node list length = : " + nodeList.getLength());
                for (int i = 0; i < nodeList.getLength(); i++)
                {
                    Node n1 = nodeList.item(i);
                    //Log.i("name", "node name = " + n1.getNodeName());
                    NodeList nodelist = n1.getChildNodes();
                    String name = "";
                    double lat = 0, lon = 0;
                    int mucdo = 0;
                    boolean active = false;
                    for (int j = 0; j < nodelist.getLength(); j++){
                        String nodename = nodelist.item(j).getNodeName();

                        if (nodename.equalsIgnoreCase("Name")){
                            name = nodelist.item(j).getTextContent();
                        }
                        if (nodename.equalsIgnoreCase("Lat")){
                            lat = Double.parseDouble(nodelist.item(j).getTextContent());
                        }
                        if (nodename.equalsIgnoreCase("Lon")){
                            lon = Double.parseDouble(nodelist.item(j).getTextContent());
                        }
                        if (nodename.equalsIgnoreCase("MucDo")){
                            mucdo = Integer.parseInt(nodelist.item(j).getTextContent());
                        }
                        if (nodename.equalsIgnoreCase("Active")){
                            active = (nodelist.item(j).getTextContent().equalsIgnoreCase("true"));
                        }
                    }
                    if (active){
                    ViTriNguyHiem vitri = new ViTriNguyHiem(new LatLng(lat,lon),name,0,mucdo,2);
                    ListViTriKetXe.add(vitri);
                    }
                }
                isListViTriUnTacUpdated = true;
            }

            MapsActivity.hashCodeUnTac = newHashCodeUnTac;
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return ListViTriKetXe;
    }
}
