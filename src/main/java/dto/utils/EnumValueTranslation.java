package dto.utils;

import dto.enums.DestinationArea;
import dto.enums.DeviceCategory;

import static dto.enums.DestinationArea.*;
import static dto.enums.DeviceCategory.*;

public class EnumValueTranslation {

    public static String convertFromDeviceCategory(DeviceCategory deviceCategory) {
        String retVal = null;
        switch (deviceCategory){
            case DEVICE_DETECTION:
                retVal = "x";
                break;
            case AV_RECEIVER:
                retVal = "1";
                break;
            case STEREO_RECEIVER:
                retVal = "2";
                break;
            default:
                retVal = "Invalid device category";
                break;
        }
        return retVal;
    }

    public static DeviceCategory convertToDeviceCategory(String value) {
        if (value.equals("x")) {
            return DEVICE_DETECTION;
        } else if (value.equals("1")) {
            return AV_RECEIVER;
        } else if (value.equals("2")) {
            return STEREO_RECEIVER;
        } else {
            return null;
        }
    }

    public static String convertFromDestinationArea(DestinationArea destinationArea) {
        String retVal = null;
        switch (destinationArea){
            case NORTH_AMERICAN_MODEL:
                retVal = "DX";
                break;
            case EUROPEAN_ASIAN_MODEL:
                retVal = "XX";
                break;
            case JAPANESE_MODEL:
                retVal = "JJ";
                break;
            default:
                retVal = "Invalid destination area";
                break;
        }
        return retVal;
    }

    public static DestinationArea convertToDestinationArea(String value) {
        if (value.equals("DX")) {
            return NORTH_AMERICAN_MODEL;
        } else if (value.equals("XX")) {
            return EUROPEAN_ASIAN_MODEL;
        } else if (value.equals("JJ")) {
            return JAPANESE_MODEL;
        } else {
            return null;
        }
    }

}
