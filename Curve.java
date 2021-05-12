import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import java.io.Serializable;
public class Curve extends ArrayList<Point>
{
    private SerializableColor color; //Color is not serializable
    private double width; //width of pen that draws curve
    public Curve(Color color, double width)
    {
        this.color = SerializableColor.makeSerializableColor(color);
        this.width = width;
    }
    public void draw(GraphicsContext pen)
    {
        pen.setStroke(color.getFXColor());
        pen.beginPath(); //starts path
        pen.setLineWidth(width);
        pen.setLineCap(StrokeLineCap.ROUND);
        pen.setLineJoin(StrokeLineJoin.ROUND);
        pen.moveTo(get(0).getX(), get(0).getY());
        //nothing for the last point to join to
        for(int k = 0; k < size() - 1; k++)
        {
            pen.lineTo(get(k+1).getX(), get(k+1).getY());
        }
        pen.stroke();
    }

}
