//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.15 at 09:10:07 PM BST 
//


package pl.baczkowicz.mqttspy.versions.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UpdateStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UpdateStatus"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ON_LATEST"/&gt;
 *     &lt;enumeration value="NEW_AVAILABLE"/&gt;
 *     &lt;enumeration value="UPDATE_RECOMMENDED"/&gt;
 *     &lt;enumeration value="CRITICAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "UpdateStatus")
@XmlEnum
public enum UpdateStatus {

    ON_LATEST,
    NEW_AVAILABLE,
    UPDATE_RECOMMENDED,
    CRITICAL;

    public String value() {
        return name();
    }

    public static UpdateStatus fromValue(String v) {
        return valueOf(v);
    }

}
