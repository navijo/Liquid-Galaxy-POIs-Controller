package com.gsoc.ijosa.liquidgalaxycontroller.utils;

import android.app.Activity;
import android.util.Xml;
import android.widget.Toast;

import com.gsoc.ijosa.liquidgalaxycontroller.PW.model.POI;
import com.gsoc.ijosa.liquidgalaxycontroller.PW.model.Point;
import com.gsoc.ijosa.liquidgalaxycontroller.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Josa on 26/05/16.
 */
public class CustomXmlPullParser {

    private static final String ns = null;

    public List<POI> parse(InputStream in, Activity activity) throws IOException {
        try {
            org.xmlpull.v1.XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readPOIS(parser);
        } catch (XmlPullParserException e) {
            Toast.makeText(activity, activity.getResources().getString(R.string.parsingError), Toast.LENGTH_LONG).show();
        } finally {
            in.close();
        }
        return new ArrayList<>();
    }

    private List<POI> readPOIS(org.xmlpull.v1.XmlPullParser parser) throws XmlPullParserException, IOException {
        List<POI> entries = new ArrayList<POI>();

        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "kml");
        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
            if (parser.getEventType() != org.xmlpull.v1.XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the folder tag
            if (name.equalsIgnoreCase("Folder")) {
                entries = readFolder(parser);
            } else if (!name.equalsIgnoreCase("Document")) {
                skip(parser);
            }
        }
        return entries;
    }

    private List<POI> readFolder(org.xmlpull.v1.XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "Folder");

        List<POI> entries = new ArrayList<>();

        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
            if (parser.getEventType() != org.xmlpull.v1.XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase("Placemark")) {
                entries.add(readPlacemark(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }


    private POI readPlacemark(org.xmlpull.v1.XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "Placemark");

        String poiName = "";
        String poiDescription = "";
        Point point = null;

        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
            if (parser.getEventType() != org.xmlpull.v1.XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase("name")) {
                poiName = readPoiName(parser);
            } else if (name.equals("description")) {
                poiDescription = readPoiDescription(parser);
            } else if (name.equalsIgnoreCase("Point")) {
                point = readPoint(parser);
            } else if (!name.equalsIgnoreCase("Placemark")) {
                skip(parser);
            }
        }
        return new POI(poiName, poiDescription, point);
    }

    // Processes the name of the placemark
    private String readPoiName(org.xmlpull.v1.XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "name");
        String title = readText(parser);
        parser.require(org.xmlpull.v1.XmlPullParser.END_TAG, ns, "name");
        return title;
    }

    // Processes the description of the placemark
    private String readPoiDescription(org.xmlpull.v1.XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(org.xmlpull.v1.XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    // Processes the point of the placemark
    private Point readPoint(org.xmlpull.v1.XmlPullParser parser) throws IOException, XmlPullParserException {
        Point point = new Point();
        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "Point");
        String tag = parser.getName();

        if (tag.equalsIgnoreCase("Point")) {
            point = readCoordinates(parser);
        }
        parser.require(org.xmlpull.v1.XmlPullParser.END_TAG, ns, "Point");
        return point;
    }

    private Point readCoordinates(org.xmlpull.v1.XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "Point");

        Point point = new Point();

        while (parser.next() != org.xmlpull.v1.XmlPullParser.END_TAG) {
            if (parser.getEventType() != org.xmlpull.v1.XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase("coordinates")) {
                point = readCoordinatesString(parser);
            } else {
                skip(parser);
            }
        }
        return point;

    }

    private Point readCoordinatesString(org.xmlpull.v1.XmlPullParser parser) throws IOException, XmlPullParserException {

        String coordinates;
        String[] coordinatesSplit;

        parser.require(org.xmlpull.v1.XmlPullParser.START_TAG, ns, "coordinates");
        coordinates = readText(parser);
        parser.require(org.xmlpull.v1.XmlPullParser.END_TAG, ns, "coordinates");

        coordinatesSplit = coordinates.split(",");

        return new Point(coordinatesSplit[1], coordinatesSplit[0]);
    }


    // Extract the text value of a tag
    private String readText(org.xmlpull.v1.XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == org.xmlpull.v1.XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(org.xmlpull.v1.XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != org.xmlpull.v1.XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case org.xmlpull.v1.XmlPullParser.END_TAG:
                    depth--;
                    break;
                case org.xmlpull.v1.XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
