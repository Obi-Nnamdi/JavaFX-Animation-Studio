PATH_TO_FX=/usr/local/bin/javafx-sdk-15/lib
BuildFolder=build
MainFile=Animate
# Compile the java files
javac --module-path ${PATH_TO_FX} --add-modules=javafx.controls,javafx.fxml,javafx.media,javafx.web,javafx.swing -d ${BuildFolder} ${MainFile}.java

# Run the java files
cd ${BuildFolder}
java --module-path ${PATH_TO_FX} --add-modules=javafx.controls,javafx.fxml,javafx.media,javafx.web,javafx.swing ${MainFile}
cd ..