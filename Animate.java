
/**************************************************
*   Author: Nnamdi Obi, Aarav Mehta, Cliff McGinnis
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
import javafx.scene.control.ScrollPane;
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
import javafx.animation.*;
import javafx.util.Duration;
import javafx.beans.property.*;
import javafx.scene.image.WritableImage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Animate extends Application {
    private final Canvas canvas;
    private final GraphicsContext pen;
    private Color bgColor;
    private Color currentColor;
    private ArrayList<Curve> currentDrawing;
    private ArrayList<ArrayList<Curve>> drawings;
    private Curve curve;
    private double currentWidth;
    private HBox animationBar;
    private Timeline timeline;
    private Boolean timelineRunning;
    private double frameRate;
    private ArrayList<Boolean> active;
    private Stage primary;

    public Animate() {
        timelineRunning = false;
        frameRate = 1000.0;
        timeline = new Timeline();
        canvas = new Canvas(800, 600);
        animationBar = new HBox();
        pen = canvas.getGraphicsContext2D();
        bgColor = Color.WHITE;
        currentColor = Color.BLACK;
        currentWidth = 20;
        curve = new Curve(Color.BLACK, 1);
        currentDrawing = new ArrayList<Curve>();
        drawings = new ArrayList<ArrayList<Curve>>();
        curve = null;
        active = new ArrayList<Boolean>();
    }

    @Override
    public void init() {
    }

    @Override
    public void start(Stage primary) {
        this.primary = primary;
        primary.setTitle("Animation Studio");
        BorderPane bp = new BorderPane();
        bp.setCenter(canvas);
        // animation bar
        active.add(true);
        GridPane gp = new GridPane();
        HBox bottomBar = new HBox();
        ScrollPane sp = new ScrollPane(animationBar);
        Button saveButton = new Button("Save Drawing");
        Button clearButton = new Button("Clear Drawing");
        Button playStopButton = new Button("Play");
        bp.setBottom(bottomBar);
        gp.add(animationBar, 3, 0);
        gp.add(clearButton, 0, 0);
        gp.add(playStopButton, 1, 0);
        gp.add(saveButton, 2, 0);
        bottomBar.getChildren().addAll(clearButton, playStopButton, saveButton, sp);
        saveButton.setOnAction(e -> {
            ImageView view = new ImageView(canvas.snapshot(null, null));
            view.setPreserveRatio(true);
            view.setFitHeight(40);
            ArrayList<Curve> drawing = copyDrawing(currentDrawing);
            drawings.add(drawing);
            active.add(false);
            turnOn(active, active.size() - 1);
            DrawButton button = new DrawButton(drawing, bgColor, "", view);
            animationBar.getChildren().add(button);
            button.makeActive();
            // refreshAnimationBar();
        });
        playStopButton.setOnAction(e -> {
            if (timelineRunning) {
                timeline.stop();
                playStopButton.setText("Play");
                timelineRunning = false;
            } else {
                int totalFrames = animationBar.getChildren().size();
                IntegerProperty frame = new SimpleIntegerProperty(0);
                timeline = new Timeline(
                        new KeyFrame(
                                // Duration.millis(42),
                                Duration.millis(frameRate),
                                event -> {
                                    ((DrawButton) animationBar.getChildren().get(frame.get())).makeActive();
                                    frame.set((frame.get() + 1) % totalFrames);
                                }));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                playStopButton.setText("Stop");
                timelineRunning = true;
            }
        });

        clearButton.setOnAction(e -> {
            pen.setFill(bgColor);
            pen.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            currentDrawing.clear();
            if (findActive(active) != active.size() - 1) {
                ((DrawButton) animationBar.getChildren().get(findActive(active))).setGraphic(getImage());
            }
        });
        canvas.setOnMousePressed(e -> {
            curve = new Curve(currentColor, currentWidth); // TODO: WIDTH
            currentDrawing.add(curve);
            curve.add(new Point(e.getX(), e.getY()));

        });
        canvas.setOnMouseDragged(e -> {
            curve.add(new Point(e.getX(), e.getY()));
            curve.draw(pen);
        });
        canvas.setOnMouseReleased(e -> {
            curve.add(new Point(e.getX(), e.getY()));
            curve.draw(pen);
            if (findActive(active) != active.size() - 1) {
                ((DrawButton) animationBar.getChildren().get(findActive(active))).setGraphic(getImage());
            }
        });
        bp.setTop(buildMenus());
        primary.setScene(new Scene(bp));
        saveButton.fire();
        primary.show();
    }

    private MenuBar buildMenus() {
        MenuBar mbar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem quitItem = new MenuItem("Quit");
        Menu saveMenu = new Menu("Save");
        MenuItem saveDrawItem = new MenuItem("Save Drawing...");
        MenuItem saveAnimItem = new MenuItem("Save Animation...");
        saveMenu.getItems().addAll(saveDrawItem, saveAnimItem);
        Menu openMenu = new Menu("Open");
        MenuItem openDrawItem = new MenuItem("Open Drawing...");
        MenuItem openAnimItem = new MenuItem("Open Animation...");
        openMenu.getItems().addAll(openDrawItem, openAnimItem);
        MenuItem clearItem = new MenuItem("Clear All");
        openDrawItem.setOnAction(e -> deserializeDrawing());
        openAnimItem.setOnAction(e -> deserializeAnim());
        saveDrawItem.setOnAction(e -> serializeDrawing());
        saveAnimItem.setOnAction(e -> serializeAnim());
        quitItem.setOnAction(e -> Platform.exit());
        clearItem.setOnAction(e -> {
            animationBar.getChildren().clear();
            currentDrawing.clear();
            drawings.clear();
            pen.setFill(Color.WHITE);
            pen.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        });
        fileMenu.getItems().addAll(quitItem, saveMenu, openMenu, clearItem);

        Menu colorMenu = new Menu("Color");
        colorMenu.getItems().addAll(
                new ColorMenuItem(Color.LIGHTBLUE, "Light Blue"),
                new ColorMenuItem(Color.BLUE, "Blue"),
                new ColorMenuItem(Color.GREEN, "Green"),
                new ColorMenuItem(Color.ORANGE, "Orange"),
                new ColorMenuItem(Color.GREEN, "Green"),
                new ColorMenuItem(Color.RED, "Red"));
        MenuItem customColor = new MenuItem("Custom...");
        colorMenu.getItems().add(customColor);
        customColor.setOnAction(c -> {
            Stage popup = new Stage();
            Pane p = new Pane();
            popup.setScene(new Scene(p));
            ColorPicker cp = new ColorPicker();
            p.getChildren().add(cp);
            popup.show();
            cp.setOnAction(e -> {
                currentColor = cp.getValue();
                popup.close();
            });
        });
        Menu fMenu = new Menu("Frame Rate");
        MenuItem fPicker = new MenuItem("Frame Rate Picker");
        fPicker.setOnAction(e -> {
            Stage popup = new Stage();
            BorderPane p = new BorderPane();
            HBox hbox = new HBox();
            Button doneButton = new Button("Done");
            Slider size = new Slider(1, 60, 1000.0 / frameRate);
            size.setSnapToTicks(true);
            Label number = new Label("Current: " + 1000.0 / frameRate);
            size.valueProperty().addListener((observable, oldValue, newValue) -> {
                number.setText(String.format("Frame Rate: %.1f", newValue.doubleValue()));
                frameRate = 1000. / newValue.doubleValue();
            });
            hbox.getChildren().addAll(number, size, doneButton);
            p.setTop(hbox);
            popup.setScene(new Scene(p, 300, 200));
            popup.show();
            doneButton.setOnAction(f -> popup.close());
        });
        fMenu.getItems().add(fPicker);
        Menu bgMenu = new Menu("Background");
        bgMenu.getItems().addAll(
                new BgMenuItem(Color.PINK, "PINK"),
                new BgMenuItem(Color.LIGHTBLUE, "LIGHTBLUE"),
                new BgMenuItem(Color.WHITE, "WHITE"),
                new BgMenuItem(Color.BLACK, "BLACK"));

        MenuItem customBgColor = new MenuItem("Custom...");
        bgMenu.getItems().add(customBgColor);
        customBgColor.setOnAction(c -> {
            Stage popup = new Stage();
            Pane p = new Pane();
            popup.setScene(new Scene(p));
            ColorPicker cp2 = new ColorPicker();
            p.getChildren().add(cp2);
            popup.show();
            cp2.setOnAction(e -> {
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
        chooseItem.setOnAction(e -> {
            Stage popup = new Stage();
            BorderPane p = new BorderPane();
            HBox hbox = new HBox();
            Button doneButton = new Button("Done");
            Slider size = new Slider(1, 200, currentWidth);
            Label number = new Label("Width of Pen: " + currentWidth);
            size.valueProperty().addListener((observable, oldValue, newValue) -> {
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
                widthMenu, fMenu);

        return mbar;
    }

    public void refresh() {
        // draw background
        pen.setFill(bgColor);
        pen.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // redraw curves
        for (Curve c : currentDrawing) {
            c.draw(pen);
        }
    }

    public ImageView getImage() {
        ImageView view = new ImageView(canvas.snapshot(null, null));
        view.setPreserveRatio(true);
        view.setFitHeight(40);
        return view;
    }

    public void turnOn(ArrayList<Boolean> bactive, int index) {
        for (int i = 0; i < bactive.size(); i++) {
            if (i != index) {
                bactive.set(i, false);
            } else {
                bactive.set(i, true);
            }
        }

    }

    public int findActive(ArrayList<Boolean> cactive) {
        for (int i = 0; i < cactive.size(); i++) {
            if (cactive.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public void refreshAnimationBar() {
        animationBar.getChildren().clear();
        for (int i = 0; i < drawings.size(); i++) {
            ArrayList<Curve> temp = drawings.get(i);
            currentDrawing = temp;
            refresh();
            ImageView view = new ImageView(canvas.snapshot(null, null));
            view.setPreserveRatio(true);
            view.setFitHeight(40);
            ArrayList<Curve> drawing = copyDrawing(currentDrawing);
            DrawButton button = new DrawButton(drawing, bgColor, "", view);
            animationBar.getChildren().add(button);
        }
    }

    public ArrayList<Curve> copyDrawing(ArrayList<Curve> original) {
        ArrayList<Curve> copy = new ArrayList<Curve>();
        for (Curve c : original) {
            copy.add(c);
        }
        return copy;
    }

    public void serializeDrawing() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Drawing");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Draw Files", "*.draw"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showSaveDialog(primary);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile));
            oos.writeObject(
                    SerializableColor.makeSerializableColor(currentColor));
            oos.writeObject(
                    SerializableColor.makeSerializableColor(bgColor));
            oos.writeDouble(currentWidth);
            oos.writeObject(currentDrawing);
            oos.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deserializeDrawing() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Drawing");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Draw Files", "*.draw"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primary);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile));
            SerializableColor scc = (SerializableColor) ois.readObject();
            currentColor = scc.getFXColor();
            SerializableColor sbg = (SerializableColor) ois.readObject();
            bgColor = sbg.getFXColor();
            currentWidth = ois.readDouble();
            currentDrawing = (ArrayList<Curve>) ois.readObject();
            refresh();
            ImageView view = new ImageView(canvas.snapshot(null, null));
            view.setPreserveRatio(true);
            view.setFitHeight(40);
            ArrayList<Curve> drawing = copyDrawing(currentDrawing);
            drawings.add(drawing);
            DrawButton button = new DrawButton(drawing, bgColor, "", view);
            animationBar.getChildren().add(button);
            ois.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void serializeAnim() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Animation");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Animation Files", "*.anim"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showSaveDialog(primary);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile));
            oos.writeObject(
                    SerializableColor.makeSerializableColor(currentColor));
            oos.writeObject(
                    SerializableColor.makeSerializableColor(bgColor));
            oos.writeDouble(currentWidth);
            oos.writeObject(drawings);
            oos.close();
        }

        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deserializeAnim() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Animation");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Animation Files", "*.anim"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primary);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile));
            SerializableColor scc = (SerializableColor) ois.readObject();
            currentColor = scc.getFXColor();
            SerializableColor sbg = (SerializableColor) ois.readObject();
            bgColor = sbg.getFXColor();
            currentWidth = ois.readDouble();
            drawings = (ArrayList<ArrayList<Curve>>) ois.readObject();
            refreshAnimationBar();

            ois.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() {
    }

    class DrawButton extends Button {
        private final ArrayList<Curve> drawing;
        final Color selfBgColor;

        public DrawButton(ArrayList<Curve> drawing, Color selfBgColor, String name, ImageView image) {
            super(name, image);
            this.drawing = copyDrawing(drawing);
            this.selfBgColor = selfBgColor;
            System.out.printf("Generated drawing %s that had %s curves\n", name, drawing.size());
            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY || e.isControlDown()) {
                    int i = animationBar.getChildren().indexOf(this);
                    animationBar.getChildren().remove(i);
                    drawings.remove(i);
                    active.remove(i);
                    if (i > 0)
                        i--;
                    if (animationBar.getChildren().isEmpty()) {
                        bgColor = Color.WHITE;
                        currentDrawing = new ArrayList<Curve>();
                        refresh();
                    } else
                        ((DrawButton) animationBar.getChildren().get(i)).makeActive();
                    turnOn(active, i);
                } else
                    makeActive();
                turnOn(active, animationBar.getChildren().indexOf(this));
            });
        }

        public ArrayList<Curve> getDrawing() {
            return drawing;
        }

        public void makeActive() {
            currentDrawing = drawing;
            bgColor = selfBgColor;
            System.out.printf("Used drawing %s that had %s curves\n", getText(), drawing.size());
            turnOn(active, animationBar.getChildren().indexOf(this));
            refresh();
        }
    }

    class ColorMenuItem extends MenuItem {
        private final Color color;

        public ColorMenuItem(Color color, String name) {
            super(name);
            this.color = color;
            setOnAction(e -> {
                currentColor = color;
            });
        }

    }

    class BgMenuItem extends MenuItem {
        private final Color color;

        public BgMenuItem(Color color, String name) {
            super(name);
            this.color = color;
            setOnAction(e -> {
                bgColor = color;
                refresh();
            });
        }
    }

    class widthMenuItem extends MenuItem {
        private final Double width;

        public widthMenuItem(Double width) {
            super("" + width);
            this.width = width;
            setOnAction(e -> {
                currentWidth = width;
            });
        }
    }
}
