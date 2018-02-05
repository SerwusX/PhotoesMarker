package aei.polsl.pl.photoesmarker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import java.text.SimpleDateFormat;

import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Calendar;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Andrzej on 2017-11-23.
 */

public class PhotoMarker {

    //private String imageFilePath;
    private final String gpsLocationProvider = LocationManager.GPS_PROVIDER;
    private final String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

    /////////////////////  WSZYSTKIE TAGI, KTÓRE SĄ UŻYWANE W PROGRAMIE /////////////////////
    // * TAG_GPS_LATITUDE - szerokość geograficzna
    // * TAG_GPS_LONGITUDE - długość geograficzna
    // * TAG_ORIENTATION - orientacja zdjęcia
    // * TAG_DATETIME - czas wykonania zdjęcia
    // * TAG_GPS_LATITUDE_REF - półkulka S/N
    // * TAG_GPS_LONGITUDE_REF - półkula E/W
    // * TAG_GPS_ALTITUDE - wysokość nad poziomem morza
    // * TAG_GPS_ALTITUDE_REF - czy nad poziomem morza          <- nieużywany w sortowaniu
    // * TAG_IMAGE_LENGTH - długość zdjęcia                     <- nieużywany w sortowaniu
    // * TAG_IMAGE_WIDTH - szerokość zdjęcia                    <- nieużywany w sortowaniu
    // * TAG_MAKE - zawiera ocenę i jakość zdjęcia
    // * TAG_GPS_PROCESSING_METHOD - zawiera dane żyroskopowe
    /////////////////////////////////////////////////////////////////////////////////////////

    public static String getNameStr(String imageFilePath){
        int index = imageFilePath.lastIndexOf("/");
        return imageFilePath.substring(index + 1);
    }

    public static String getLatitudeStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String latitudeInExifFormat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            return convertPositionToNormalFormat(latitudeInExifFormat) + getLatitudeRefStr(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy pusty string
        return "";
    }

    public static String getLongitudeStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String longitudeInExifFormat = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            return convertPositionToNormalFormat(longitudeInExifFormat) + getLongitudeRefStr(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy pusty string
        return "";
    }

    public static String getOrientationStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String orientationStr =  exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            final int orientation = Integer.parseInt(orientationStr);
            switch (orientation){
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    return "Flip horizontal";
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return "Flip vertical";
                case ExifInterface.ORIENTATION_NORMAL:
                    return "Normal";
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return "Rotate 90";
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return "Rotate 180";
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return "Rotate 270";
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    return "Transpose";
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    return "Transverse";
                default:
                case ExifInterface.ORIENTATION_UNDEFINED:
                    return "Undefined";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy "niezdefiniowany"
        return "Undefined";
    }

    public static String getDateTimeStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy pusty string
        return "";
    }

    private static String getLatitudeRefStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            if (latitudeRef != null)
                return " " + latitudeRef;
            else
                return "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy pusty string
        return "";
    }

    private static String getLongitudeRefStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if (longitudeRef != null)
                return " " + longitudeRef;
            else
                return "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy pusty string
        return "";
    }

    private static String getAltitudeStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            int altitude = exif.getAttributeInt(ExifInterface.TAG_GPS_LATITUDE, Integer.MAX_VALUE);
            if (altitude == Integer.MAX_VALUE)
                return "";
            else
                return String.valueOf(altitude) + getAltitudeRefStr(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            //W przypadku błędów odczytu zwracamy pusty string
            return "";
        }
    }

    private static String getAltitudeRefStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            int altitudeRef = exif.getAttributeInt(ExifInterface.TAG_GPS_ALTITUDE_REF, -1);

            if (altitudeRef == 1)
                return " pod poziomem morza";
            else if (altitudeRef == 0)
                return " nad poziomem morza";
            else
                return "";
        } catch (IOException e) {
            e.printStackTrace();
            //W przypadku błędów odczytu zwracamy pusty string
            return "";
        }
    }

    public static String getPhotoLengthStr(String imageFilePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imageFilePath, options);
        int length = options.outHeight;

        return String.valueOf(length);
    }

    public static String getPhotoWidthStr(String imageFilePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imageFilePath, options);
        int width = options.outWidth;

        return String.valueOf(width);
    }

    public static String getPhotoQualityStr(String imageFilePath){
        int quality = PhotoEvaluator.getPhotoQuality(imageFilePath);
        if(quality != 0)
            return String.valueOf(quality);
        else
            return "";
    }

    public static String getPhotoRatingStr(String imageFilePath){
        int rating = PhotoEvaluator.getPhotoRating(imageFilePath);
        if(rating != 0)
            return String.valueOf(rating);
        else
            return "";
    }

    public static String getAverageOfRatingAndQualityStr(String imageFilePath){
        int rating = PhotoEvaluator.getPhotoRating(imageFilePath);
        int quality = PhotoEvaluator.getPhotoQuality(imageFilePath);
        int average = (rating + quality) / 2;
        if(average != 0)
            return String.valueOf(average);
        else
            return "";
    }

    public static String getGyroZValueStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String gyroValuesStr = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
            String[] parts = gyroValuesStr.split(",");
            return parts[2];
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (NullPointerException e) {
            return "";
        } catch (ArrayIndexOutOfBoundsException e){
            return "";
        }
    }

    public static String getGyroYValueStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String gyroValuesStr = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
            String[] parts = gyroValuesStr.split(",");
            return parts[1];
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (NullPointerException e) {
            return "";
        } catch (ArrayIndexOutOfBoundsException e){
            return "";
        }
    }

    public static String getGyroXValueStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            String gyroValuesStr = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
            String[] parts = gyroValuesStr.split(",");
            return parts[0];
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (NullPointerException e) {
            return "";
        } catch (ArrayIndexOutOfBoundsException e){
            return "";
        }
    }

    //To nie będzie raczej potrzebne, ale na wszelki wypadek zostawiam
    private static String getAllGyroValuesAsOneStr(String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            return exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //W przypadku błędów odczytu zwracamy pusty string
        return "";
    }

    //Dodaje markery do zdjęcia
    public void addMarkersToPhoto(Context context, String imageFilePath, double gyroX, double gyroY, double gyroZ) {
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);

            //Za każdym razem jest sprawdzane, czy tag nie jest już dodany i ew. dodanie go

            //Orientacja zdjęcia (obrót podczas robienia zdjęcia)
            String orientationCurrentTag = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (TextUtils.isEmpty(orientationCurrentTag)) {
                String orientationDetectedByApplication = readOrientation(context);
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientationDetectedByApplication);
                exif.saveAttributes();
            }

            //Data i godzina wykonania zdjęcia
            String dateTimeCurrentTag = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (TextUtils.isEmpty(dateTimeCurrentTag)) {
                String dateTimeOfTheDevice = readDateTime();
                exif.setAttribute(ExifInterface.TAG_DATETIME, dateTimeOfTheDevice);
                exif.saveAttributes();
            }

            //Długość zdjęcia w pikselach
            String imageLengthCurrentTag = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            if (TextUtils.isEmpty(imageLengthCurrentTag)) {
                String lengthOfTheImage = readImageLength(imageFilePath);
                exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, lengthOfTheImage);
                exif.saveAttributes();
            }

            //Szerokość zdjęcia w pikselach
            String imageWidthCurrentTag = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            if (TextUtils.isEmpty(imageWidthCurrentTag)) {
                String widthOfTheImage = readImageWidth(imageFilePath);
                exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, widthOfTheImage);
                exif.saveAttributes();
            }

            //GPS - szerokość geograficzna
            String GPSLatitudeCurrentTag = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String GPSLatitudeOfTheDevice = "";
            if (TextUtils.isEmpty(GPSLatitudeCurrentTag)) {
                GPSLatitudeOfTheDevice = readGPSLatitude(context);

                if(GPSLatitudeOfTheDevice != null){
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSLatitudeOfTheDevice);
                    exif.saveAttributes();
                }
            }

            //GPS - półkula N/S
            String GPSLatitudeRefCurrentTag = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            if (TextUtils.isEmpty(GPSLatitudeRefCurrentTag)){
                if (!TextUtils.isEmpty(GPSLatitudeOfTheDevice)){
                    double valueOfLatitudeOfTheDevice = Double.parseDouble(GPSLatitudeOfTheDevice);
                    if(valueOfLatitudeOfTheDevice < 0.0f)
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                    else
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                    exif.saveAttributes();
                }
            }

            //GPS - długość geograficzna
            String GPSLongitudeCurrentTag = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String GPSLongitudeOfTheDevice = "";
            if (TextUtils.isEmpty(GPSLongitudeCurrentTag)) {
                GPSLongitudeOfTheDevice = readGPSLongitude(context);
                if(GPSLongitudeOfTheDevice != null){
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSLongitudeOfTheDevice);
                    exif.saveAttributes();
                }
            }

            //GPS - półkula W/E
            String GPSLongitudeRefCurrentTag = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if (TextUtils.isEmpty(GPSLongitudeRefCurrentTag)){
                if (!TextUtils.isEmpty(GPSLongitudeOfTheDevice)){
                    double valueOfLongitudeOfTheDevice = Double.parseDouble(GPSLongitudeOfTheDevice);
                    if(valueOfLongitudeOfTheDevice < 0.0f)
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                    else
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                    exif.saveAttributes();
                }
            }


            //GPS - wysokość nad poziomem morza w metrach
            String GPSAltitudeCurrentTag = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            if (TextUtils.isEmpty(GPSAltitudeCurrentTag)) {
                String GPSAltitudeOfTheDevice = readGPSAltitude(context);
                if(GPSAltitudeOfTheDevice != null){
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, GPSAltitudeOfTheDevice);
                    exif.saveAttributes();
                    if(Integer.parseInt(GPSAltitudeOfTheDevice) >= 0)
                        //0 w tagu, to nad poziomem morza
                        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "0");
                    else
                        //1 w tagu, to pod poziomem morza
                        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "1");
                    exif.saveAttributes();
                }
            }


            //Orientacja na podstawie żyroskopu - tutaj nie sprawdzamy, czy coś jest w tagu
            //Format zapisu to będzie: "x,y,z" <- gdzie x to azimuth, y to pitch, z to roll
            String gyroValuesStr =
                    String.valueOf(gyroX) + "," + String.valueOf(gyroY) + "," + String.valueOf(gyroZ);

            exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, gyroValuesStr);
            exif.saveAttributes();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Błąd w PhotoMarker", e.toString());
        }

    }

    private String readOrientation(Context context) {
        final int orientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (orientation) {
            case Surface.ROTATION_0:
                return Integer.toString(ExifInterface.ORIENTATION_NORMAL);
            case Surface.ROTATION_90:
                return Integer.toString(ExifInterface.ORIENTATION_ROTATE_90);
            case Surface.ROTATION_180:
                return Integer.toString(ExifInterface.ORIENTATION_ROTATE_180);
            default:
            case Surface.ROTATION_270:
                return Integer.toString(ExifInterface.ORIENTATION_ROTATE_270);
        }
    }

    private String readDateTime() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:mm:dd hh:mm:ss");
        String formattedDate = simpleDateFormat.format(currentTime);

        return formattedDate;
    }

    private String readImageLength(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        String length = Integer.toString(options.outHeight);

        return length;
    }

    private String readImageWidth(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        String width = Integer.toString(options.outWidth);

        return width;
    }

    private String readGPSLatitude(Context context) {
        String latitude;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            latitude = null;
            return latitude;
        }
        else{
            Location lastKnownLocationByGPS =
                    locationManager.getLastKnownLocation(gpsLocationProvider);
            Location lastKnownLocationByNetwork =
                    locationManager.getLastKnownLocation(networkLocationProvider);


            Log.d("GPS", "GPS" + lastKnownLocationByGPS.toString());
            Log.d("NET", "NET" + lastKnownLocationByNetwork.toString());

            if(lastKnownLocationByGPS == null){
                if(lastKnownLocationByNetwork == null){
                    latitude = null;
                    return latitude;
                }
                else {
                    latitude = convertPositionToExifFormat(lastKnownLocationByNetwork.getLatitude());
                    return latitude;
                }
            }
            else {
                latitude = convertPositionToExifFormat(lastKnownLocationByGPS.getLatitude());
                return latitude;
            }
        }
    }

    private String readGPSLongitude(Context context){
        String longitude;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            longitude = null;
            return longitude;
        }
        else{
            Location lastKnownLocationByGPS =
                    locationManager.getLastKnownLocation(gpsLocationProvider);
            Location lastKnownLocationByNetwork =
                    locationManager.getLastKnownLocation(networkLocationProvider);

            if(lastKnownLocationByGPS == null){
                if(lastKnownLocationByNetwork == null){
                    longitude = null;
                    return longitude;
                }
                else {
                    longitude = convertPositionToExifFormat(lastKnownLocationByNetwork.getLongitude());
                    return longitude;
                }
            }
            else {
                longitude = convertPositionToExifFormat(lastKnownLocationByGPS.getLongitude());
                return longitude;
            }
        }
    }

    private String readGPSAltitude(Context context){
        String altitude;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            altitude = null;
            return altitude;
        }
        else{
            Location lastKnownLocationByGPS =
                    locationManager.getLastKnownLocation(gpsLocationProvider);
            Location lastKnownLocationByNetwork =
                    locationManager.getLastKnownLocation(networkLocationProvider);
            if(lastKnownLocationByGPS == null){
                if(lastKnownLocationByNetwork == null){
                    altitude = null;
                    return altitude;
                }
                else {
                    altitude = Double.toString(lastKnownLocationByNetwork.getAltitude());
                    return altitude;
                }
            }
            else {
                altitude = Double.toString(lastKnownLocationByGPS.getAltitude());
                return altitude;
            }
        }
    }

    @NonNull
    private String convertPositionToExifFormat(double position) {
        StringBuilder stringBuilder = new StringBuilder(20);
        position=Math.abs(position);
        int degree = (int) position;
        position *= 60;
        position -= (degree * 60.0d);
        int minute = (int) position;
        position *= 60;
        position -= (minute * 60.0d);
        int second = (int) (position*1000.0d);

        stringBuilder.setLength(0);
        stringBuilder.append(degree);
        stringBuilder.append("/1,");
        stringBuilder.append(minute);
        stringBuilder.append("/1,");
        stringBuilder.append(second);
        stringBuilder.append("/10000");


        return stringBuilder.toString();
    }

    private static String convertPositionToNormalFormat(String positionInExifFormatStr){
        try{
            //Format zapisu to num1/denom1,num2/denom2,num3/denom

            //Najpierw zmienne zostają rozdzielone do formatu num/denom
            String[] parts = positionInExifFormatStr.split(",");

            //Tu jest ok
            // >>> 18/1,27/1,389296/10000 <<<
            Log.d("GPS", "GPS" + positionInExifFormatStr);


            //Rozdzielenie na num i denom
            String[] partsDegrees = parts[0].split("/");
            String[] partsMinutes = parts[1].split("/");
            String[] partsSeconds = parts[2].split("/");

            Log.d("stopnie", partsDegrees[0]);
            Log.d("minuty", partsMinutes[0]);
            Log.d("sekundy", partsSeconds[0]);

            double degrees = Double.parseDouble(partsDegrees[0]) / Double.parseDouble(partsDegrees[1]);
            double minutes = Double.parseDouble(partsMinutes[0]) / Double.parseDouble(partsMinutes[1]);
            double seconds = Double.parseDouble(partsSeconds[0]) / Double.parseDouble(partsSeconds[1]);

            Log.d("stopnie2", String.valueOf(Double.parseDouble(partsDegrees[0]) / Double.parseDouble(partsDegrees[1])));
            Log.d("minuty2", String.valueOf(Double.parseDouble(partsMinutes[0]) / Double.parseDouble(partsMinutes[1])));
            Log.d("sekundy2", String.valueOf(Double.parseDouble(partsSeconds[0]) / Double.parseDouble(partsSeconds[1])));

            String finalValue = String.valueOf(degrees) + "° " + String.valueOf(minutes) + "' " + String.valueOf(seconds) + "''";
            return finalValue;

            //return String.valueOf(degrees + minutes + seconds);

        }catch (NullPointerException e){
            return "";
        }
    }

}












