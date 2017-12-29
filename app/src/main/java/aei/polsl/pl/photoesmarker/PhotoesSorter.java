package aei.polsl.pl.photoesmarker;

import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.support.v4.util.Pair;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andrzej on 28.11.2017.
 */

public class PhotoesSorter {

    //Sortowanie
    public static List<String> sortListOfPhotoes(List<String> listOfPaths, SortingTag sortingTag) throws IOException {

        //Lista zawierająca pary wartości: ścieżka - exif
        List<Pair<String, ExifInterface>> listOfPairsOfPathsAndExifs = new ArrayList<>();
        List<String> sortedList;

        if(listOfPaths.isEmpty()){
            sortedList = new ArrayList<>();
            return sortedList;
        }

        for (String e : listOfPaths){
            ExifInterface exif = new ExifInterface(e);
            Pair<String, ExifInterface> pair = new Pair<>(e, exif);
            listOfPairsOfPathsAndExifs.add(pair);
        }

        switch (sortingTag){
            case ORIENTATION:
                sortedList = sortByOrientation(listOfPairsOfPathsAndExifs);
                break;
            case DATE:
                sortedList = sortByDate(listOfPairsOfPathsAndExifs);
                break;
            case PHOTO_LENGTH:
                sortedList = sortByPhotoLength(listOfPairsOfPathsAndExifs);
                break;
            case PHOTO_WIDTH:
                sortedList = sortByPhotoWidth(listOfPairsOfPathsAndExifs);
                break;
            case PHOTO_AREA:
                sortedList = sortByPhotoArea(listOfPairsOfPathsAndExifs);
                break;
            case GPS_POSITION:
                sortedList = sortByGPSPosition(listOfPairsOfPathsAndExifs);
                break;
            case GPS_SEA_LEVEL:
                sortedList = sortBySeaLevel(listOfPairsOfPathsAndExifs);
                break;
            case GYROSCOPE:
                sortedList = sortByGyroscope(listOfPairsOfPathsAndExifs);
                break;
            case RATING:
                sortedList = sortByRating(listOfPairsOfPathsAndExifs);
                break;
            case QUALITY:
                sortedList = sortByQuality(listOfPairsOfPathsAndExifs);
                break;
            case AVERAGE_RATING_AND_QUALITY:
                sortedList = sortByAverageRatingAndQuality(listOfPairsOfPathsAndExifs);
                break;
            default:
            case NAME:
                sortedList = sortByName(listOfPairsOfPathsAndExifs);
                break;
        }

        return sortedList;
    }

    //Sortowanie po pozycji GPS - gotowe, do przetestowania
    private static List<String> sortByGPSPosition(List<Pair<String, ExifInterface>> list) {

        //Najpierw wstępne sortowania - szukamy punktu najbardziej wysuniętego na NW
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                final double startPointX = 0;
                final double startPointY = 0;

                double x1 = convertToDegree(pair1.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                double y1 = convertToDegree(pair1.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

                String gpsLatitudeRef1;
                if(pair1.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) == null)
                    gpsLatitudeRef1 = "N";
                else
                    gpsLatitudeRef1 = pair1.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

                String gpsLongitudeRef1;
                if(pair1.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) == null)
                    gpsLongitudeRef1 = "E";
                else
                    gpsLongitudeRef1 = pair1.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                if(gpsLatitudeRef1.equals("S"))
                    x1 = -x1;
                if(gpsLongitudeRef1.equals("W"))
                    y1 = -y1;

                double distanceFromStartPoint1 = calculateDistanceBetweenTwoPoints(startPointX, startPointY, x1, y1);

                double x2 = convertToDegree(pair2.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                double y2 = convertToDegree(pair2.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

                String gpsLatitudeRef2;
                if(pair2.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) == null)
                    gpsLatitudeRef2 = "N";
                else
                    gpsLatitudeRef2 = pair2.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

                String gpsLongitudeRef2;
                if(pair2.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) == null)
                    gpsLongitudeRef2 = "E";
                else
                    gpsLongitudeRef2 = pair2.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                if(gpsLatitudeRef2.equals("S"))
                    x2 = -x2;
                if(gpsLongitudeRef2.equals("W"))
                    y2 = -y2;

                double distanceFromStartPoint2 = calculateDistanceBetweenTwoPoints(startPointX, startPointY, x2, y2);

                return Double.compare(distanceFromStartPoint1, distanceFromStartPoint2);
            }
        });

        //Lista, w której znajdą się posortowane elementy
        List<Pair<String, ExifInterface>> sortedListOfPairs = new ArrayList<>();

        //Dodanie pierwszego elementu o minimalnej odległości do posortowanej listy i usunięcie go z listy nieposortowanej
        sortedListOfPairs.add(list.remove(0));

        //Ostatni element z listy, w której już są posortowane elementy
        Pair<String, ExifInterface> lastElementOfSortedList;

        //Tymczasowy, poprzedni minimalny element z nieposortowanej listy
        Pair<String, ExifInterface> currentMinElementOfList;

        while (!list.isEmpty()) {

            //Pobranie ostatniego elementu z posortowanej listy
            lastElementOfSortedList = sortedListOfPairs.get(sortedListOfPairs.size() - 1);

            //Pobranie pierwszego elementu z nieposortowanej listy
            currentMinElementOfList = list.get(0);

            //Indeks obecnego elementu z nieposortowanej listy
            int currentMinIndexOfElementOfList = 0;

            double xOfLastElementOfSortedList = convertToDegree(lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            double yOfLastElementOfSortedList = convertToDegree(lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

            String tmpLastSortedLatitudeRef;
            if(lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) == null)
                tmpLastSortedLatitudeRef = "N";
            else
                tmpLastSortedLatitudeRef = lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

            String tmpLastSortedLongitudeRef;
            if(lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) == null)
                tmpLastSortedLongitudeRef = "E";
            else
                tmpLastSortedLongitudeRef = lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if(tmpLastSortedLatitudeRef.equals("S"))
                xOfLastElementOfSortedList = -xOfLastElementOfSortedList;
            if(tmpLastSortedLongitudeRef.equals("W"))
                yOfLastElementOfSortedList = -yOfLastElementOfSortedList;

            double xOfCurrentElement = convertToDegree(currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            double yOfCurrentElement = convertToDegree(currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

            String tmpCurrentSortedLatitudeRef;
            if(currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) == null)
                tmpCurrentSortedLatitudeRef = "N";
            else
                tmpCurrentSortedLatitudeRef = currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

            String tmpCurrentSortedLongitudeRef;
            if(currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) == null)
                tmpCurrentSortedLongitudeRef = "E";
            else
                tmpCurrentSortedLongitudeRef = currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if(tmpCurrentSortedLatitudeRef.equals("S"))
                xOfCurrentElement = -xOfCurrentElement;
            if(tmpCurrentSortedLongitudeRef.equals("W"))
                yOfCurrentElement = -yOfCurrentElement;

            double distanceOfPreviousElement = calculateDistanceBetweenTwoPoints(
                    xOfLastElementOfSortedList,
                    yOfLastElementOfSortedList,
                    xOfCurrentElement,
                    yOfCurrentElement
            );

            for (int i = 0; i < list.size(); i++){

                xOfCurrentElement = convertToDegree(currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                yOfCurrentElement = convertToDegree(currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

                if(tmpCurrentSortedLatitudeRef.equals("S"))
                    xOfCurrentElement = -xOfCurrentElement;
                if(tmpCurrentSortedLongitudeRef.equals("W"))
                    yOfCurrentElement = -yOfCurrentElement;

                double distanceOfCurrentElement = calculateDistanceBetweenTwoPoints(
                        xOfLastElementOfSortedList,
                        yOfLastElementOfSortedList,
                        xOfCurrentElement,
                        yOfCurrentElement
                );

                if (distanceOfCurrentElement <= distanceOfPreviousElement){
                    currentMinIndexOfElementOfList = i;
                }
                distanceOfPreviousElement = distanceOfCurrentElement;
            }

            currentMinElementOfList = list.remove(currentMinIndexOfElementOfList);
            sortedListOfPairs.add(currentMinElementOfList);

        }

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : sortedListOfPairs){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Liczy odległość między dwoma punktami
    private static double calculateDistanceBetweenTwoPoints(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((x2-x1), 2)+Math.pow((y2-y1), 2));
    }

    //Zamienia format położenia GPS EXIF na stopnie
    private static Double convertToDegree(String locationExifFormat){
        try{
            Double result = null;
            String[] dms = locationExifFormat.split(",", 3);

            String[] stringD = dms[0].split("/", 2);
            Double d0 = new Double(stringD[0]);
            Double d1 = new Double(stringD[1]);
            Double doubleD = d0/d1;

            String[] stringM = dms[1].split("/", 2);
            Double m0 = new Double(stringM[0]);
            Double m1 = new Double(stringM[1]);
            Double doubleM = m0/m1;

            String[] stringS = dms[2].split("/", 2);
            Double s0 = new Double(stringS[0]);
            Double s1 = new Double(stringS[1]);
            Double doubleS = s0/s1;

            result = new Double(doubleD + (doubleM/60) + (doubleS/3600));

            return result;
        }
        catch (RuntimeException e){
            return Double.MAX_VALUE;
        }
    }

    //Sortowanie po dacie - gotowe, do przetestowania
    private static List<String> sortByDate(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                String stringDate1 = pair1.second.getAttribute(ExifInterface.TAG_DATETIME);
                String stringDate2 = pair2.second.getAttribute(ExifInterface.TAG_DATETIME);

                DateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);

                Date initialDate = new Date(2100, 01,01);

                Date date1 = new Date();
                try {
                    date1 = format.parse(stringDate1);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    date1 = initialDate;
                }

                Date date2 = new Date();
                try {
                    date2 = format.parse(stringDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){

                }

                return date1.compareTo(date2);
            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po orientacji zdjęcia - gotowe, do przetestowania
    private static List<String> sortByOrientation(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                int orientation1 = pair1.second.getAttributeInt(ExifInterface.TAG_ORIENTATION, Integer.MAX_VALUE);
                int orientation2 = pair2.second.getAttributeInt(ExifInterface.TAG_ORIENTATION, Integer.MAX_VALUE);

                return (orientation1 - orientation2);

            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po poziomie nad poziomem morza - gotowe, do przetestowania
    private static List<String> sortBySeaLevel(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                int seaLevel1 = pair1.second.getAttributeInt(ExifInterface.TAG_GPS_ALTITUDE, Integer.MAX_VALUE);
                int seaLevel2 = pair2.second.getAttributeInt(ExifInterface.TAG_GPS_ALTITUDE, Integer.MAX_VALUE);

                return seaLevel1 - seaLevel2;
            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po rozdzielczości zdjęcia - gotowe, do przetestowania
    private static List<String> sortByPhotoArea(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                String pathStr1 = pair1.first;
                String pathStr2 = pair2.first;

                BitmapFactory.decodeFile(pathStr1, options);

                int width1 = options.outWidth;
                int length1 = options.outHeight;
                long area1 = length1 * width1;

                if (area1 == 0)
                    area1 = Long.MAX_VALUE;

                BitmapFactory.decodeFile(pathStr2, options);

                int width2 = options.outWidth;
                int length2 = options.outHeight;
                long area2 = length2 * width2;

                if (area2 == 0)
                    area2 = Long.MAX_VALUE;

                return (int)(area2-area1);
            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po szerokości zdjęcia - gotowe, do przetestowania
    private static List<String> sortByPhotoWidth(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                String pathStr1 = pair1.first;
                String pathStr2 = pair2.first;

                BitmapFactory.decodeFile(pathStr1, options);
                int width1 = options.outWidth;

                BitmapFactory.decodeFile(pathStr2, options);
                int width2 = options.outWidth;

                if (width1 == 0)
                    width1 = Integer.MAX_VALUE;

                if (width2 == 0)
                    width2 = Integer.MAX_VALUE;

                return width2 - width1;
            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po długości zdjęcia - gotowe, do przetestowania
    private static List<String> sortByPhotoLength(List<Pair<String, ExifInterface>> list){

        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                String pathStr1 = pair1.first;
                String pathStr2 = pair2.first;

                BitmapFactory.decodeFile(pathStr1, options);
                int length1 = options.outHeight;

                BitmapFactory.decodeFile(pathStr2, options);
                int length2 = options.outHeight;

                if (length1 == 0)
                    length1 = Integer.MAX_VALUE;

                if (length2 == 0)
                    length2 = Integer.MAX_VALUE;

                return length2 - length1;
            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po nazwie zdjęcia - gotowe, do przetestowania
    private static List<String> sortByName(List<Pair<String, ExifInterface>> list){

        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                return  pair1.first.substring(pair1.first.lastIndexOf("/")+1).compareTo(
                        pair2.first.substring(pair2.first.lastIndexOf("/")+1));

            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po odczytach z żyroskopu
    private static List<String> sortByGyroscope(List<Pair<String, ExifInterface>> list){

        //Najpierw wstępne sortowania - szukamy punktu najbliżesz początku układu
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                //Punkt startowy to [0.0, 0.0, 0.0]
                final double startPointX = 0.0;
                final double startPointY = 0.0;
                final double startPointZ = 0.0;

                String gyroValuesStr1 = pair1.second.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

                double x1;
                double y1;
                double z1;

                if(gyroValuesStr1 != null){
                    String[] parts1 = gyroValuesStr1.split(",");
                    x1 = Double.parseDouble(parts1[0]);
                    y1 = Double.parseDouble(parts1[1]);
                    z1 = Double.parseDouble(parts1[2]);
                }else{
                    x1 = Double.MAX_VALUE;
                    y1 = Double.MAX_VALUE;
                    z1 = Double.MAX_VALUE;
                }

                double distanceFromStartPoint1 = calculateDistanceBetweenTwoPointsIn3DSystem(
                        startPointX,
                        startPointY,
                        startPointZ,
                        x1,
                        y1,
                        z1
                );

                String gyroValuesStr2 = pair2.second.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

                double x2;
                double y2;
                double z2;

                if(gyroValuesStr1 != null){
                    String[] parts2 = gyroValuesStr2.split(",");
                    x2 = Double.parseDouble(parts2[0]);
                    y2 = Double.parseDouble(parts2[1]);
                    z2 = Double.parseDouble(parts2[2]);
                }else{
                    x2 = Double.MAX_VALUE;
                    y2 = Double.MAX_VALUE;
                    z2 = Double.MAX_VALUE;
                }

                double distanceFromStartPoint2 = calculateDistanceBetweenTwoPointsIn3DSystem(
                        startPointX,
                        startPointY,
                        startPointZ,
                        x2,
                        y2,
                        z2
                );

                return Double.compare(distanceFromStartPoint1, distanceFromStartPoint2);
            }
        });



        //Lista, w której znajdą się posortowane elementy
        List<Pair<String, ExifInterface>> sortedListOfPairs = new ArrayList<>();

        //Dodanie pierwszego elementu o minimalnej odległości do posortowanej listy i usunięcie go z listy nieposortowanej
        sortedListOfPairs.add(list.remove(0));

        //Ostatni element z listy, w której już są posortowane elementy
        Pair<String, ExifInterface> lastElementOfSortedList;

        //Tymczasowy, poprzedni minimalny element z nieposortowanej listy
        Pair<String, ExifInterface> currentMinElementOfList;

        while (!list.isEmpty()) {

            //Pobranie ostatniego elementu z posortowanej listy
            lastElementOfSortedList = sortedListOfPairs.get(sortedListOfPairs.size() - 1);

            //Pobranie pierwszego elementu z nieposortowanej listy
            currentMinElementOfList = list.get(0);

            //Indeks obecnego elementu z nieposortowanej listy
            int currentMinIndexOfElementOfList = 0;

            String sortedGyroValuesStr = lastElementOfSortedList.second.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

            String[] parts1 = sortedGyroValuesStr.split(",");

            double xOfLastElementOfSortedList = Double.parseDouble(parts1[0]);
            double yOfLastElementOfSortedList = Double.parseDouble(parts1[1]);
            double zOfLastElementOfSortedList = Double.parseDouble(parts1[2]);

            String currentGyroValuesStr = currentMinElementOfList.second.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

            double xOfCurrentElement;
            double yOfCurrentElement;
            double zOfCurrentElement;
            String[] parts2;
            if(currentGyroValuesStr != null){
                parts2 = currentGyroValuesStr.split(",");

                xOfCurrentElement = Double.parseDouble(parts2[0]);
                yOfCurrentElement = Double.parseDouble(parts2[1]);
                zOfCurrentElement = Double.parseDouble(parts2[2]);
            }
            else {
                xOfCurrentElement = Double.MAX_VALUE;
                yOfCurrentElement = Double.MAX_VALUE;
                zOfCurrentElement = Double.MAX_VALUE;
            }

            double distanceOfPreviousElement = calculateDistanceBetweenTwoPointsIn3DSystem(
                    xOfLastElementOfSortedList,
                    yOfLastElementOfSortedList,
                    zOfLastElementOfSortedList,
                    xOfCurrentElement,
                    yOfCurrentElement,
                    zOfCurrentElement
            );

            for (int i = 0; i < list.size(); i++){


                currentGyroValuesStr = list.get(i).second.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

                if(currentGyroValuesStr != null){
                    parts2 = currentGyroValuesStr.split(",");

                    xOfCurrentElement = Double.parseDouble(parts2[0]);
                    yOfCurrentElement = Double.parseDouble(parts2[1]);
                    zOfCurrentElement = Double.parseDouble(parts2[2]);
                }
                else {
                    xOfCurrentElement = Double.MAX_VALUE;
                    yOfCurrentElement = Double.MAX_VALUE;
                    zOfCurrentElement = Double.MAX_VALUE;
                }


                double distanceOfCurrentElement = calculateDistanceBetweenTwoPointsIn3DSystem(
                        xOfLastElementOfSortedList,
                        yOfLastElementOfSortedList,
                        zOfLastElementOfSortedList,
                        xOfCurrentElement,
                        yOfCurrentElement,
                        zOfCurrentElement
                );

                if (distanceOfCurrentElement <= distanceOfPreviousElement){
                    currentMinIndexOfElementOfList = i;
                }
                distanceOfPreviousElement = distanceOfCurrentElement;
            }

            currentMinElementOfList = list.remove(currentMinIndexOfElementOfList);
            sortedListOfPairs.add(currentMinElementOfList);
        }

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : sortedListOfPairs){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Liczy odległość między dwoma punktami w przestrzeni trójwymiarowej
    private static double calculateDistanceBetweenTwoPointsIn3DSystem(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.sqrt(Math.pow((x2-x1), 2) + Math.pow((y2-y1), 2) + Math.pow((z2-z1), 2));
    }

    //Sortowanie po ocenach zdjęć
    private static List<String> sortByRating(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                int rating1 = PhotoEvaluator.getPhotoRating(pair1.first);
                int rating2 = PhotoEvaluator.getPhotoRating(pair2.first);
                return rating2 - rating1;

            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po jakości zdjęć
    private static List<String> sortByQuality(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                int quality1 = PhotoEvaluator.getPhotoQuality(pair1.first);
                int quality2 = PhotoEvaluator.getPhotoQuality(pair2.first);

                return quality2 - quality1;

            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }

    //Sortowanie po średniej ocen i jakości zdjęć
    private static List<String> sortByAverageRatingAndQuality(List<Pair<String, ExifInterface>> list){
        Collections.sort(list, new Comparator<Pair<String, ExifInterface>>() {
            @Override
            public int compare(Pair<String, ExifInterface> pair1, Pair<String, ExifInterface> pair2) {

                int quality1 = PhotoEvaluator.getPhotoQuality(pair1.first);
                int quality2 = PhotoEvaluator.getPhotoQuality(pair2.first);

                int rating1 = PhotoEvaluator.getPhotoRating(pair1.first);
                int rating2 = PhotoEvaluator.getPhotoRating(pair2.first);

                int average1 = (quality1 + rating1) / 2;
                int average2 = (quality2 + rating2) / 2;

                return average2 - average1;

            }
        });

        List<String> sortedList = new ArrayList<>();

        for (Pair<String, ExifInterface> e : list){
            sortedList.add(e.first);
        }

        return sortedList;
    }
}
