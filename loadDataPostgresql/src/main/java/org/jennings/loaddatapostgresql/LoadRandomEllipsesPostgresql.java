/*
 * (C) Copyright 2017 David Jennings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     David Jennings
 */

/*
Created for a single purpose.  Load random ellipses into a Postgresql Table.

This class uses geotools to create the Ellipse.  They are commented out for now.

Doesn't really belong in Simulator; but I'm leaving it here for now until I find a better home.

 */
package org.jennings.loaddatapostgresql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import org.jennings.geotools.Ellipse;
import org.jennings.geotools.GeographicCoordinate;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class LoadRandomEllipsesPostgresql {

    private static ArrayList<GeographicCoordinate> landGrids = null;

    private String loadLandGrids() {
        landGrids = new ArrayList<>();

        String message = "";

        try {
            FileReader fr = new FileReader("landgrids.csv");
            BufferedReader br = new BufferedReader(fr);

            br.readLine();  // discard first line header
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] parts = strLine.split(",");
                Double lat = Double.parseDouble(parts[0]);
                Double lon = Double.parseDouble(parts[1]);
                GeographicCoordinate coord = new GeographicCoordinate(lon, lat);
                landGrids.add(coord);
            }

        } catch (Exception e) {
            message = "ERROR" + e.getClass() + ">>" + e.getMessage();
            System.out.println(message);
        }
        return message;
    }

    private void run() {
        try {

            Connection c;
            Statement stmt;
            c = DriverManager
                    .getConnection("jdbc:postgresql://pg95:5432/gis1",
                            "user1", "user1");
            c.setAutoCommit(false);

            
            stmt = c.createStatement();
            String sql = "select max(oid) maxoid from ellipse;";
            ResultSet rs = stmt.executeQuery(sql);
            
            rs.next();
            
            int oid = rs.getInt("maxoid");

            
            int numPoints = 50;

            loadLandGrids();

            int numLandGrids = landGrids.size();

            JSONObject crs = new JSONObject();
            crs.put("type", "name");
            JSONObject nm = new JSONObject();
            nm.put("name", "EPSG:4326");
            crs.put("properties", nm);
            
            //System.out.println(crs);
            
            int num = 0;
            
            while (num < 1000) {
                
                num +=1;
                oid +=1;
            
                Random rnd = new Random();

                double a = rnd.nextDouble() * 1 + 0.01;  // a from 0.01 to 1.1km
                double b = rnd.nextDouble() * 1 + 0.01;  // b from 0.01 to 1.1km
                double r = rnd.nextDouble() * 360; // Rotation 0 to 360 

                GeographicCoordinate llcorner = landGrids.get(rnd.nextInt(numLandGrids));

                double minLon = llcorner.getLon();
                double maxLon = minLon + 1;
                double lon = minLon + (maxLon - minLon) * rnd.nextDouble();

                double minLat = llcorner.getLat();
                double maxLat = minLat + 1;

                double lat = minLat + (maxLat - minLat) * rnd.nextDouble();

                JSONObject result = new JSONObject();
                JSONArray polys = new JSONArray();
                try {

                    // Problem with large areas crossing -180 and 180 for now I'll set to small number
                    Ellipse ellipse = new Ellipse();
                    polys = ellipse.createEllipse(lon, lat, a, b, r, numPoints, false);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                JSONObject geom = new JSONObject();
                geom.put("type", "MultiPolygon");
                geom.put("coordinates", polys);
                geom.put("crs", crs);

                sql = "INSERT INTO ellipse (oid,a,b,clat,clon,rot,num,geom) VALUES (";
                sql += oid + ",";
                sql += a + ",";
                sql += b + ",";
                sql += lat + ",";
                sql += lon + ",";
                sql += r + ",";
                sql += num + ",";
                sql += "ST_GeomFromGeoJSON('" + geom.toString() + "'));";
                //System.out.println(sql);
                
                stmt = c.createStatement();
                stmt.executeUpdate(sql);                
                
                if (num % 1000 == 0) c.commit();
                
            }

            c.commit();

            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        LoadRandomEllipsesPostgresql t = new LoadRandomEllipsesPostgresql();
        t.run();

    }

}
