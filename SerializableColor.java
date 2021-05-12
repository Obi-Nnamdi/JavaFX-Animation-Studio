/**************************************************
*   Author: Nnamdi Obi
*   Date:  5 Nov 2020
*   Date last modified: 5 Nov 2020
**************************************************/

import javafx.application.Application;
import java.io.Serializable;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class SerializableColor implements Serializable
{
    private double red;
    private double green;
    private double blue;
    private double alpha;
    public SerializableColor(Color color)
    {
        this.red = color.getRed();
        this.blue = color.getBlue();
        this.green = color.getGreen();
        this.alpha = color.getOpacity();

    }
    public SerializableColor(double red, double blue, double green, double alpha)
    {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;

    }
    public static SerializableColor makeSerializableColor(Color c)
    {
        return new SerializableColor(c.getRed(), c.getBlue(), c.getGreen(), c.getOpacity());
    }
    private int intColorLevel(double d)
    {
        return (int)(Math.round(255*d));
    }
    private String twoDigit(int n)
    {
        String out = Integer.toString(n, 16);
        return n < 16? "0" + out: out;
    }
    public String makeHexCode()
    {
        return String.format("#%s%s%s", twoDigit(intColorLevel(red)),
        twoDigit(intColorLevel(green)), twoDigit(intColorLevel(blue)));
    }
    public Color getFXColor()
    {
        return new Color(red, green, blue, alpha);
    }
    public static void main(String[] args)
    {
        Color c = Color.ORANGE;
        SerializableColor sc = new SerializableColor(c);
        System.out.println(sc.makeHexCode());
    }

}
