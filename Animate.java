/**************************************************
*   Author: Morrison
*   Date:  03 Nov 202020
**************************************************/

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import java.util.ArrayList;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

public class Animate extends Application
{
    private final Canvas canvas;
    private final GraphicsContext pen;
    private Color bgColor;
    private Color currentColor;
    private ArrayList<Curve> currentDrawing;
    private ArrayList<ArrayList<Curve>> drawings;
    private Curve curve;
    private double currentWidth;
    private GridPane animationBar;
    public Animate()
    {
        canvas = new Canvas(800, 600);
        animationBar = new GridPane();
        pen = canvas.getGraphicsContext2D();
        bgColor = Color.WHITE;
        currentColor = Color.BLACK;
        currentWidth = 20;
        curve = new Curve(Color.BLACK, 1);
        currentDrawing = new ArrayList<Curve>();
        drawings = new ArrayList<ArrayList<Curve>>();
        curve = null;
    }

    @Override
    public void init()
    {
    }

    @Override
    public void start(Stage primary)
    {
        primary.setTitle("Animation Studio");
        BorderPane bp = new BorderPane();
        bp.setCenter(canvas);
        //animation bar
        GridPane gp = new GridPane();
        
        Button saveButton = new Button("Save Drawing");
        Button clearButton = new Button("Clear Drawing");
        Button playButton = new Button("Play");
        bp.setBottom(gp);
        gp.add(animationBar, 3,0);
        gp.add(clearButton, 0, 0);
        gp.add(playButton, 1, 0);
        gp.add(saveButton, 2, 0);
        saveButton.setOnAction( e ->
        {
            drawings.add(copyDrawing(currentDrawing));
            animationBar.add(new DrawButton(drawings.get(drawings.size() - 1), bgColor, "" + drawings.size()),drawings.size() - 1,0);
            //refreshAnimationBar();
        });
        clearButton.setOnAction(e ->{
            pen.setFill(bgColor);
            pen.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
            currentDrawing.clear();
        });
        canvas.setOnMousePressed( e ->
        {
            curve = new Curve(currentColor, currentWidth); //TODO: WIDTH
            currentDrawing.add(curve);
            curve.add(new Point(e.getX(), e.getY()));

        });
        canvas.setOnMouseDragged( e ->
        {
            curve.add(new Point(e.getX(), e.getY()));
            curve.draw(pen);
        });
        canvas.setOnMouseReleased( e ->
        {
            curve.add(new Point(e.getX(), e.getY()));
            curve.draw(pen);
        });
        bp.setTop(buildMenus());
        primary.setScene(new Scene(bp));
        primary.show();
    }

    private MenuBar buildMenus()
    {
        MenuBar mbar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem quitItem = new MenuItem("Quit");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem clearItem = new MenuItem("Clear All");
        openItem.setOnAction(e -> deserialize());
        saveItem.setOnAction(e -> serialize());
        quitItem.setOnAction(e -> Platform.exit());
        clearItem.setOnAction(e ->{    
            for (ArrayList<Curve> r : drawings) {
                r.clear();
            }
            animationBar.getChildren().clear();
            drawings.clear();
            pen.setFill(Color.WHITE);
            pen.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        });
        fileMenu.getItems().addAll(quitItem, saveItem, openItem,clearItem);

        Menu colorMenu = new Menu("Color");
        colorMenu.getItems().addAll(
            new ColorMenuItem(Color.LIGHTBLUE, "Light Blue"),
            new ColorMenuItem(Color.BLUE, "Blue"),
            new ColorMenuItem(Color.GREEN, "Green"),
            new ColorMenuItem(Color.ORANGE, "Orange"),
            new ColorMenuItem(Color.GREEN, "Green"),
            new ColorMenuItem(Color.RED, "Red")
        );
        MenuItem customColor = new MenuItem("Custom...");
        colorMenu.getItems().add(customColor);
        customColor.setOnAction(c ->
        {
            Stage popup = new Stage();
            Pane p = new Pane();
            popup.setScene(new Scene(p));
            ColorPicker cp = new ColorPicker();
            p.getChildren().add(cp);
            popup.show();
            cp.setOnAction(e ->
            {
                currentColor = cp.getValue();
                popup.close();
            });
        });
        Menu bgMenu = new Menu("Background");
        bgMenu.getItems().addAll(
            new BgMenuItem(Color.PINK, "PINK"),
            new BgMenuItem(Color.LIGHTBLUE, "LIGHTBLUE"),
            new BgMenuItem(Color.WHITE, "WHITE"),
            new BgMenuItem(Color.BLACK, "BLACK")
        );

        MenuItem customBgColor = new MenuItem("Custom..."); 
        bgMenu.getItems().add(customBgColor);
        customBgColor.setOnAction(c ->
        {
            Stage popup = new Stage();
            Pane p = new Pane();
            popup.setScene(new Scene(p));
            ColorPicker cp2 = new ColorPicker();
            p.getChildren().add(cp2);
            popup.show();
            cp2.setOnAction(e ->
            {
                bgColor = cp2.getValue();
                refresh();
                popup.close();
            });
        });
        Menu widthMenu = new Menu("Width");
        MenuItem chooseItem = new MenuItem("Choose...");
        widthMenu.getItems().addAll(
        new widthMenuItem(1.0),
        new widthMenuItem(5.0),
        new widthMenuItem(10.0),
        new widthMenuItem(20.0),
        new widthMenuItem(50.0),
        chooseItem);
        chooseItem.setOnAction(e ->
        {
            Stage popup = new Stage();
            BorderPane p = new BorderPane();
            HBox hbox = new HBox();
            Button doneButton = new Button("Done");
            Slider size = new Slider(1, 200, currentWidth);
            Label number = new Label("Width of Pen: " + currentWidth);
            size.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                number.setText("Width of Pen: " + newValue.intValue());
                currentWidth = newValue.intValue();
            });
            hbox.getChildren().addAll(number, size, doneButton);
            p.setTop(hbox);
            popup.setScene(new Scene(p, 300, 200));
            popup.show();
            doneButton.setOnAction(f -> popup.close());
        });
        mbar.getMenus().addAll(fileMenu, colorMenu, bgMenu,
            widthMenu);

        return mbar;
    }
    public void refresh()
    {
        //draw background
        pen.setFill(bgColor);
        pen.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //redraw curves
        for(Curve c: currentDrawing)
        {
            c.draw(pen);
        }
    }

    // public void refreshAnimationBar()
    // {
    //     animationBar.getChildren().clear();
    //     for (int i = 0; i < drawings.size(); i++)
    //     {
    //         ArrayList<Curve> drawing = drawings.get(i);
    //         animationBar.getChildren().add(new DrawButton(drawing, "" + i));
    //     }
    // }
    public ArrayList<Curve> copyDrawing(ArrayList<Curve> original)
    {
        ArrayList<Curve> copy = new ArrayList<Curve>();
        for(Curve c : original)
        {
            copy.add(c);
        }
        return copy;
    }

    public void serialize()
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("draw.test")));
            oos.writeObject(
            SerializableColor.makeSerializableColor(currentColor));
            oos.writeObject(
            SerializableColor.makeSerializableColor(bgColor));
            oos.writeDouble(currentWidth);
            oos.writeObject(currentDrawing);
            oos.close();


        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    public void deserialize()
    {
        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("draw.test")));
            SerializableColor scc = (SerializableColor) ois.readObject();
            currentColor = scc.getFXColor();
            SerializableColor sbg = (SerializableColor) ois.readObject();
            bgColor = sbg.getFXColor();
            currentWidth = ois.readDouble();
            currentDrawing = (ArrayList<Curve>) ois.readObject();
            refresh();
            ois.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
    }
    class DrawButton extends Button
    {
        private final ArrayList<Curve> drawing;
        final Color bgColor;
        public DrawButton(ArrayList<Curve> drawing, Color bgColor, String name)
        {
            super(name);
            this.drawing = copyDrawing(drawing);
            this.bgColor = bgColor;
            System.out.printf("Generated drawing %s that had %s curves\n", name, drawing.size());
            setOnAction(e ->
            {
                currentDrawing = drawing;
                System.out.printf("Used drawing %s that had %s curves\n", name, drawing.size());
                refresh();
            });
        } 
    }
    class ColorMenuItem extends MenuItem
    {
        private final Color color;
        public ColorMenuItem(Color color, String name)
        {
            super(name);
            this.color = color;
            setOnAction(e ->
            {
                currentColor = color;
            });
        }

    }
    class BgMenuItem extends MenuItem
    {
        private final Color color;
        public BgMenuItem(Color color, String name)
        {
            super(name);
            this.color = color;
            setOnAction(e ->
            {
                bgColor = color;
                refresh();
            });
        }
    }
    class widthMenuItem extends MenuItem
    {
        private final Double width;
        public widthMenuItem(Double width)
        {
            super("" + width);
            this.width = width;
            setOnAction(e ->
            {
                currentWidth = width;
            });
        }
    }
}
